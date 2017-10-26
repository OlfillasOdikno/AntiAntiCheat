package de.olfillasodikno.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.jdi.VirtualMachineManagerService;

import sun.tools.attach.LinuxAttachProvider;
import sun.tools.attach.LinuxVirtualMachine;
import sun.tools.attach.SolarisAttachProvider;
import sun.tools.attach.SolarisVirtualMachine;
import sun.tools.attach.WindowsAttachProvider;
import sun.tools.attach.WindowsVirtualMachine;

public class AgentMain {

	public static final String MC_MAIN_CLASS = "net.minecraft.client.main.Main";
	public static final String MSG_SUCC = "Agent injected to [%s]";

	public static void agentmain(final String agentArgs, final Instrumentation inst) {

	}

	public static void main(String[] args) {
		String jar_file = AgentMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		jar_file = jar_file.substring(1);
		extractLoader(jar_file);
		OS os = OS.getOS();
		if (os == OS.WIN) {
			WindowsAttachProvider provider = new WindowsAttachProvider();
			ArrayList<VirtualMachineDescriptor> minecraft_vms = new ArrayList<>();
			Iterator<VirtualMachineDescriptor> it = provider.listVirtualMachines().iterator();

			while (it.hasNext()) {
				VirtualMachineDescriptor descriptor = it.next();
				if (!descriptor.displayName().startsWith(MC_MAIN_CLASS)) {
					continue;
				}
				minecraft_vms.add(descriptor);
			}
			for (VirtualMachineDescriptor desc : minecraft_vms) {
				try {
					VirtualMachine vm = provider.attachVirtualMachine(desc.id());
					vm.loadAgent(jar_file, MC_MAIN_CLASS);
					System.out.println(String.format(MSG_SUCC, desc.id()));
					vm.detach();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (os == OS.LIN) {
			LinuxAttachProvider provider = new LinuxAttachProvider();
			ArrayList<VirtualMachineDescriptor> minecraft_vms = new ArrayList<>();
			Iterator<VirtualMachineDescriptor> it = provider.listVirtualMachines().iterator();

			while (it.hasNext()) {
				VirtualMachineDescriptor descriptor = it.next();
				if (!descriptor.displayName().startsWith(MC_MAIN_CLASS)) {
					continue;
				}
				minecraft_vms.add(descriptor);
			}
			for (VirtualMachineDescriptor desc : minecraft_vms) {
				try {
					VirtualMachine vm = provider.attachVirtualMachine(desc.id());
					vm.loadAgent(jar_file, MC_MAIN_CLASS);
					System.out.println(String.format(MSG_SUCC, desc.id()));
					vm.detach();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (os == OS.SOL) {
			SolarisAttachProvider provider = new SolarisAttachProvider();
			ArrayList<VirtualMachineDescriptor> minecraft_vms = new ArrayList<>();
			Iterator<VirtualMachineDescriptor> it = provider.listVirtualMachines().iterator();

			while (it.hasNext()) {
				VirtualMachineDescriptor descriptor = it.next();
				if (!descriptor.displayName().startsWith(MC_MAIN_CLASS)) {
					continue;
				}
				minecraft_vms.add(descriptor);
			}
			for (VirtualMachineDescriptor desc : minecraft_vms) {
				try {
					VirtualMachine vm = provider.attachVirtualMachine(desc.id());
					vm.loadAgent(jar_file, MC_MAIN_CLASS);
					System.out.println(String.format(MSG_SUCC, desc.id()));
					vm.detach();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void extractLoader(String parentFile) {
		File parent = new File(parentFile).getParentFile();
		if (parent == null) {
			System.out.println("ERROR getting Parent File");
			return;
		}
		File folder = new File(parent, "natives");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		OS os = OS.getOS();
		ARCH arch = ARCH.getArch();
		if (arch == ARCH.NOT_FOUND || os == OS.NOT_FOUND) {
			System.out.println("ERROR");
			return;
		}
		String path = "/natives/";
		if (arch == ARCH.x64) {
			path += "64/";
		} else if (arch == ARCH.x86) {
			path += "32/";
		}
		if (os == OS.WIN) {
			path += "windows/attach.dll";
		} else if (os == OS.LIN) {
			path += "linux/libattach.so";
		} else if (os == OS.SOL) {
			path += "solaris/libattach.so";
		} else if (os == OS.MAC) {
			if (arch == ARCH.x86) {
				System.out.println("ERROR NO MAC arch x86");
				return;
			}
			path += "mac/libattach.dylib";
		}
		File outFile = new File(folder, new File(path).getName());
		if (!outFile.exists()) {
			outFile.getParentFile().mkdirs();
		}
		try {
			InputStream is = AgentMain.class.getResourceAsStream(path);
			FileOutputStream fos = new FileOutputStream(outFile);
			byte[] buf = new byte[4096];
			int r;
			while ((r = is.read(buf, 0, buf.length)) != -1) {
				fos.write(buf, 0, r);
			}
			fos.close();
			addToPath(folder.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addToPath(String path)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		if (System.getProperty("java.library.path") != null) {
			System.setProperty("java.library.path",
					path + System.getProperty("path.separator") + System.getProperty("java.library.path"));
		} else {
			System.setProperty("java.library.path", path);
		}
		final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible(true);
		fieldSysPath.set(null, null);
	}
}