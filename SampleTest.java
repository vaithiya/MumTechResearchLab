/**
 * This is class is used for test the ImmutableQueue
 * 
 * Author: Vaithiyanadhan
 * Version: 1.0
 */
public class SampleTest {
	public static void main(String args[]) {
		ImmutableQueue<String> im = new ImmutableQueue<String>();
		im = im.enQueue("manmathan");
		im = im.enQueue("raman");
		im = im.enQueue("koran");
		im = im.enQueue("raghu");
		im = im.enQueue("thimir");
		im = im.enQueue("thool");
		im = im.enQueue("raja");
		im = im.enQueue("kandam");
		im = im.deQueue();
		im = im.deQueue();
		for (int i=0; i < im.size(); i ++) {
			System.out.println(im.get(i));
		}
	}
}