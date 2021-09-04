import java.util.ArrayList;

/**
 * symbol, literal�� ���õ� �����Ϳ� ������ �����Ѵ�. section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class LabelTable {
	ArrayList<String> label;
	ArrayList<Integer> locationList;
	// external ���� �� ó������� �����Ѵ�.

	 ArrayList<Character> XorC; //X�� C�� �Ǵ��ϱ� ���� ����
	 
	  public LabelTable() {
	        label = new ArrayList<>();
	        locationList = new ArrayList<>();
	        XorC = new ArrayList<>();
	    }
	/**
	 * ���ο� symbol�� literal�� table�� �߰��Ѵ�.
	 * 
	 * @param label    : ���� �߰��Ǵ� symbol Ȥ�� literal�� lable
	 * @param location : �ش� symbol Ȥ�� literal�� ������ �ּҰ� ���� : ���� �ߺ��� symbol, literal��
	 *                 putName�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. ��Ī�Ǵ� �ּҰ��� ������
	 *                 modifylable()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putName(String label, int location) {

		this.label.add(label);
        locationList.add(location);
	}

	/**
	 * ������ �����ϴ� symbol, literal ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * 
	 * @param lable       : ������ ���ϴ� symbol, literal�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifyName(String lable, int newLocation) {
		 locationList.set(label.indexOf(lable), newLocation);
	}

	/**
	 * ���ڷ� ���޵� symbol, literal�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * 
	 * @param label : �˻��� ���ϴ� symbol Ȥ�� literal�� label
	 * @return address: ������ �ִ� �ּҰ�. �ش� symbol, literal�� ���� ��� -1 ����
	 */
	public int search(String label) {
		int address;
        //�ɺ��� �ִ��� Ȯ��
        int index = this.label.indexOf(label);
        //�ִٸ� locationList���� location �� ��������
        if (index == -1)
            address = -1;
        else
            address = locationList.get(index);

        return address;
	}

}
