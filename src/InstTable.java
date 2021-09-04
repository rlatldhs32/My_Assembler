import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ� ���� instruction ���� ����,
 * ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/**
	 * inst.data ������ �ҷ��� �����ϴ� ����. ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * 
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */

	public InstTable(String instFile)
	{
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}

	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	
	public void openFile(String fileName)
	{	
		String[] splitedStr = new String[3];
		try
		{
		File file = new File(fileName);
        FileReader filereader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(filereader);


			String line = null;

			splitedStr = null;
			
		while((line = bufReader.readLine()) != null)
			{

				
				splitedStr = null;
				splitedStr = line.split("\t");

				Instruction Inst = new Instruction(line);
				
				instMap.put(splitedStr[0], Inst);
			}
			Instruction inst2;
			filereader.close();
		
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}

/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����. instruction�� ���õ� ������ �����ϰ� �������� ������
 * �����Ѵ�.
 */
class Instruction {
	/*
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 * 
	 * ex) String instruction; int opcode; int numberOfOperand; String comment;
	 */
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format; //�켱 format�� �������� ���� 4���ľ���
	int opcode;
	int Num_operand;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * 
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	
	public Instruction(String line)
	{
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * 
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	
	public void parsing(String line)   // ���� �ֵ��� ����������
	{
		// TODO Auto-generated method stub
		String[] splitedStr;
		
		splitedStr = line.split("\t");
		
		format=Integer.valueOf(splitedStr[1]);
		opcode=Integer.valueOf(splitedStr[2],16);
		Num_operand=Integer.valueOf(splitedStr[3]);
			

		
	}
	
	// �� �� �Լ� ���� ����

}
