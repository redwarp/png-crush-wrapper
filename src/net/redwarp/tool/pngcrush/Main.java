package net.redwarp.tool.pngcrush;


public class Main {
	public static void main(String[] args) {
		DropFrame frame = new DropFrame();
		frame.setVisible(true);

		System.out.println(ClassLoader.getSystemClassLoader().getResource(".")
				.getPath());

	}
}
