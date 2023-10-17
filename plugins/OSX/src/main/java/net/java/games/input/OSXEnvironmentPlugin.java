/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materails provided with the distribution.
 *
 * Neither the name Sun Microsystems, Inc. or the names of the contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANT OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMEN, ARE HEREBY EXCLUDED.  SUN MICROSYSTEMS, INC. ("SUN") AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS
 * A RESULT OF USING, MODIFYING OR DESTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.  IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES.  HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OUR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for us in
 * the design, construction, operation or maintenance of any nuclear facility
 *
 */

package net.java.games.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * OSX HIDManager implementation
 * @author elias
 * @author gregorypierce
 * @version 1.0
 */
public final class OSXEnvironmentPlugin extends ControllerEnvironment {

	private static final Logger log = Logger.getLogger(OSXEnvironmentPlugin.class.getName());

	private static boolean supported = false;

	static {
		String osName = System.getProperty("os.name", "").trim();
log.fine(osName);
		if (osName.contains("Mac")) {
			// Could check isMacOSXEqualsOrBetterThan in here too.
			supported = true;
		}
	}

	private static boolean isMacOSXEqualsOrBetterThan(int major_required, int minor_required) {
		String os_version = System.getProperty("os.version");
		StringTokenizer version_tokenizer = new StringTokenizer(os_version, ".");
		int major;
		int minor;
		try {
			String major_str = version_tokenizer.nextToken();
			String minor_str = version_tokenizer.nextToken();
			major = Integer.parseInt(major_str);
			minor = Integer.parseInt(minor_str);
		} catch (Exception e) {
			log.fine("Exception occurred while trying to determine OS version: " + e);
			// Best guess, no
			return false;
		}
		return major > major_required || (major == major_required && minor >= minor_required);
	}

	private final Controller[] controllers;

	public OSXEnvironmentPlugin() {
		if (isSupported()) {
			this.controllers = enumerateControllers();
		} else {
log.fine("not supported");
			this.controllers = new Controller[0];
		}
	}

	@Override
	public final Controller[] getControllers() {
		return controllers;
	}

	@Override
	public boolean isSupported() {
		return supported;
	}

	private static void addElements(OSXHIDQueue queue, List<OSXHIDElement> elements, List<OSXComponent> components, boolean map_mouse_buttons) throws IOException {
		Iterator<OSXHIDElement> it = elements.iterator();
		while (it.hasNext()) {
			OSXHIDElement element = it.next();
			Component.Identifier id = element.getIdentifier();
			if (id == null)
				continue;
			if (map_mouse_buttons) {
				if (id == Component.Identifier.Button._0) {
					id = Component.Identifier.Button.LEFT;
				} else if (id == Component.Identifier.Button._1) {
					id = Component.Identifier.Button.RIGHT;
				} else if (id == Component.Identifier.Button._2) {
					id = Component.Identifier.Button.MIDDLE;
				}
			}
			OSXComponent component = new OSXComponent(id, element);
			components.add(component);
			queue.addElement(element, component);
		}
	}

	private final static Keyboard createKeyboardFromDevice(OSXHIDDevice device, List<OSXHIDElement> elements) throws IOException {
		List<OSXComponent> components = new ArrayList<>();
		OSXHIDQueue queue = device.createQueue(AbstractController.EVENT_QUEUE_DEPTH);
		try {
			addElements(queue, elements, components, false);
		} catch (IOException e) {
			queue.release();
			throw e;
		}
log.fine("@@@ components: " + components.size());
log.fine("@@@ components: " + components);
		Component[] components_array = new Component[components.size()];
		components.toArray(components_array);
		return new OSXKeyboard(device, queue, components_array, new Controller[]{}, new Rumbler[]{});
	}

	private final static Mouse createMouseFromDevice(OSXHIDDevice device, List<OSXHIDElement> elements) throws IOException {
		List<OSXComponent> components = new ArrayList<>();
		OSXHIDQueue queue = device.createQueue(AbstractController.EVENT_QUEUE_DEPTH);
		try {
			addElements(queue, elements, components, true);
		} catch (IOException e) {
			queue.release();
			throw e;
		}
log.fine("@@@ components: " + components.size());
log.fine("@@@ components: " + components);
		Component[] components_array = new Component[components.size()];
		components.toArray(components_array);
		Mouse mouse = new OSXMouse(device, queue, components_array, new Controller[]{}, new Rumbler[]{});
		if (mouse.getPrimaryButton() != null && mouse.getX() != null && mouse.getY() != null) {
			return mouse;
		} else {
			queue.release();
			return null;
		}
	}

	private final static AbstractController createControllerFromDevice(OSXHIDDevice device, List<OSXHIDElement> elements, Controller.Type type) throws IOException {
		List<OSXComponent> components = new ArrayList<>();
		OSXHIDQueue queue = device.createQueue(AbstractController.EVENT_QUEUE_DEPTH);
		try {
			addElements(queue, elements, components, false);
		} catch (IOException e) {
			queue.release();
			throw e;
		}
log.fine("@@@ components: " + components.size());
log.fine("@@@ components: " + components);
		Component[] components_array = new Component[components.size()];
		components.toArray(components_array);
		return new OSXAbstractController(device, queue, components_array, new Controller[]{}, new Rumbler[]{}, type);
	}

	private final static void createControllersFromDevice(OSXHIDDevice device, List<Controller> controllers) throws IOException {
		UsagePair usage_pair = device.getUsagePair();
		if (usage_pair == null) {
log.finer("device: '" + device.getProductName() + "' has no usage pair");
			return;
		}
log.fine("-------- device: '" + device.getProductName() + "' --------");
		List<OSXHIDElement> elements = device.getElements();
		if (usage_pair.getUsagePage() == UsagePage.GENERIC_DESKTOP && (usage_pair.getUsage() == GenericDesktopUsage.MOUSE ||
					usage_pair.getUsage() == GenericDesktopUsage.POINTER)) {
log.fine("mouse device: '" + device.getProductName() + "' --------");
			Controller mouse = createMouseFromDevice(device, elements);
			if (mouse != null)
				controllers.add(mouse);
		} else if (usage_pair.getUsagePage() == UsagePage.GENERIC_DESKTOP && (usage_pair.getUsage() == GenericDesktopUsage.KEYBOARD ||
					usage_pair.getUsage() == GenericDesktopUsage.KEYPAD)) {
log.fine("keyboard device: '" + device.getProductName() + "' --------");
			controllers.add(createKeyboardFromDevice(device, elements));
		} else if (usage_pair.getUsagePage() == UsagePage.GENERIC_DESKTOP && usage_pair.getUsage() == GenericDesktopUsage.JOYSTICK) {
log.fine("joystick device: '" + device.getProductName() + "' --------");
			controllers.add(createControllerFromDevice(device, elements, Controller.Type.STICK));
		} else if (usage_pair.getUsagePage() == UsagePage.GENERIC_DESKTOP && usage_pair.getUsage() == GenericDesktopUsage.MULTI_AXIS_CONTROLLER) {
log.fine("multi-axis device: '" + device.getProductName() + "' --------");
			controllers.add(createControllerFromDevice(device, elements, Controller.Type.STICK));
		} else if (usage_pair.getUsagePage() == UsagePage.GENERIC_DESKTOP && usage_pair.getUsage() == GenericDesktopUsage.GAME_PAD) {
log.fine("gamepad device: '" + device.getProductName() + "' --------");
			controllers.add(createControllerFromDevice(device, elements, Controller.Type.GAMEPAD));
		}
	}

	private final static Controller[] enumerateControllers() {
		List<Controller> controllers = new ArrayList<>();
		try {
			OSXHIDDeviceIterator it = new OSXHIDDeviceIterator();
			try {
				while (true) {
					OSXHIDDevice device;
					try {
						device = it.next();
						if (device == null)
							break;
						boolean device_used = false;
						try {
							int old_size = controllers.size();
							createControllersFromDevice(device, controllers);
							device_used = old_size != controllers.size();
						} catch (IOException e) {
							log.log(Level.FINE, "Failed to create controllers from device: " + device.getProductName(), e);
						}
						if (!device_used)
							device.release();
					} catch (IOException e) {
						log.log(Level.FINE, "Failed to enumerate device: ", e);
					}
				}
			} finally {
				it.close();
			}
		} catch (IOException e) {
			log.log(Level.FINE, "Failed to enumerate devices: " + e.getMessage(), e);
			return new Controller[]{};
		}
		Controller[] controllers_array = new Controller[controllers.size()];
		controllers.toArray(controllers_array);
		return controllers_array;
	}
}
