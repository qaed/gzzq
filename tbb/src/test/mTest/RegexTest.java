package mTest;

//import static org.junit.Assert.*;
//
//import org.junit.Test;

public class RegexTest {

//	@Test
	public void test() {
		String billtyes = "[642-012-za]XXX��������һ�� ��2�ٶȣ�";
		String[] billlist = new String[69];
		for (int i = 0; i < 69; i++) {
			billlist[i] = billtyes+i+"����";
		}
		long time1 = System.currentTimeMillis();
//		billtyes = billtyes.replaceAll("[\u4e00-\u9fa5]+", "");
		for (String string : billlist) {
			string.replaceAll("[\u4e00-\u9fa5]*��.*��", "");
		}
//		long time2 = System.currentTimeMillis();
//		System.out.println(time1);
//		System.out.println(time2);
//		System.out.println("��ʱ��"+(time2-time1));
//		System.out.println("��ʱ��"+69*(time2-time1));
		System.out.println(billtyes);
		StringBuffer buffer = new StringBuffer();
		if (billtyes.indexOf("[") >= 0) {
			while (billtyes.indexOf("]") >= 0) {
				if (buffer.toString().length() == 0) {
					buffer.append(billtyes.substring(billtyes.indexOf("[") + 1, billtyes.indexOf("]")));
					// 20170120 tsy ��#�ָ����������
					// ԭ��������Ϊ�� [642-012-za]XXX������
					// ���ձ任���Ϊ�� 642-012-za#XXX������
					// buffer.append("#");
					// 20170120 end
				} else {
					buffer.append("#");
					buffer.append(billtyes.substring(billtyes.indexOf("[") + 1, billtyes.indexOf("]")));
				}

				billtyes = billtyes.substring(billtyes.indexOf("]") + 1);
			}
		}
		buffer.append(billtyes);
		System.out.println(buffer.toString());
	}

}
