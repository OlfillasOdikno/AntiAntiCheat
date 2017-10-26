package de.olfillasodikno.agent;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javax.crypto.SecretKey;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class Agent {

	public static void agentmain(final String agentArgs, final Instrumentation inst) {
		if (agentArgs == null || agentArgs.isEmpty()) {
			return;
		}

		String mcMain = agentArgs.replace(".", "/");
		inst.addTransformer(new ClassFileTransformer() {

			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
					ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

				ClassReader cr = new ClassReader(classfileBuffer);
				ClassWriter cw = new ClassWriter(cr, 0);

				ClassVisitor cv = new CryptVisitor(cw);
				cr.accept(cv, 0);
				return cw.toByteArray();
			}
		}, true);
	}

	public static void save(SecretKey key) {
		String hexKey = new HexBinaryAdapter().marshal(key.getEncoded());
		JFrame frame = new JFrame();
		JButton copyBtn = new JButton("Copy to Clipboard");
		JLabel keyLab = new JLabel();
		keyLab.setText(hexKey);

		copyBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(hexKey), null);
				JOptionPane.showMessageDialog(null, "Copied", "Info", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(keyLab);
		frame.getContentPane().add(copyBtn);
		frame.pack();
		frame.setVisible(true);
	}
}