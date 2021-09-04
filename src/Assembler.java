import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Assembler: �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���η�ƾ�̴�. ���α׷��� ���� �۾��� ������ ����.
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�.
 * 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�
 * 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1)
 * 
 * 4) �м��� �������������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2)
 * 
 * 
 * �ۼ����� ���ǻ���:
 * 
 * 1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ����
 * �ȵȴ�.
 * 
 * 2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����
 * 
 * 3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 * 
 * 4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű���� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 * + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� ��
 * �ֽ��ϴ�.
 */

public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ���� */
	ArrayList<LabelTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ���� */
	ArrayList<LabelTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ���� */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. �ʿ��� ��� String ��� ������ Ŭ������
	 * �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;

	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<LabelTable>();
		literaltabList = new ArrayList<LabelTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * ������� ���� ��ƾ
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		assembler.pass1();
		assembler.printSymbolTable("symtab20172609.txt");
		assembler.printLiteralTable("littab20172609.txt");
		assembler.pass2();
		assembler.printObjectCode("output20172609.txt");
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * 
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) 
	{
		try{
        File file = new File(inputFile);
        FileReader filereader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(filereader);
        String line = "";
        while((line = bufReader.readLine()) != null){
        	lineList.add(line);
        }
        //.readLine()�� ���� ���๮�ڸ� ���� �ʴ´�.            
        bufReader.close();
    }catch (FileNotFoundException e) {
        // TODO: handle exception
    }catch(IOException e){
        System.out.println(e);
    }
	}
	

	/**
     * TokenList�� ä��� �Լ�
     */
    private void MakeTokens() {
        int Sect = 0;
        for (String line : lineList) {
            //tab�� �������� ��� �и�
            String[] arr = line.split("\t");
            		// .���� ���� �� ��
            if (arr[0].equals("."))
            	
                continue;
            // ����
            if (arr[1].equals("START"))
            {
                symtabList.add(new LabelTable());
                literaltabList.add(new LabelTable());
                TokenList.add(new TokenTable(symtabList.get(Sect),literaltabList.get(Sect), instTable));
            }
            //���ο� Section ���۽�
            if ( arr[1].equals("CSECT")) {
                Sect++;
                symtabList.add(new LabelTable());
                literaltabList.add(new LabelTable());
                TokenList.add(new TokenTable(symtabList.get(Sect),literaltabList.get(Sect), instTable));
            }
            //��ū �ֱ�
            TokenList.get(Sect).putToken(line);
            
        }
    }
	
	

	/**
	 * pass1 ������ �����Ѵ�.
	 * 
	 * 1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū ������ �и��� �� ��ū ���̺��� ����.
	 * 
	 * 2) symbol, literal ���� SymbolTable, LiteralTable�� ����.
	 * 
	 * ���ǻ���: SymbolTable, LiteralTable, TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 * 
	 * @param inputFile : input ���� �̸�.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		//token�� �����
        MakeTokens();

        //TokenList�� �̿��Ͽ� sym, literal table �ϼ��ϱ�
        boolean Is_there_Literal = false;
        int Location1;
        int LocationCounter;
        
        //�� �پ� �����鼭
        for (TokenTable section : TokenList) 
        {
        	LocationCounter = Location1 = 0;
            for (Token line : section.tokenList) {
                //�ּ� �Է� �� ���
                line.location = Location1;
                line.byteSize = getLength(line);
                LocationCounter += line.byteSize;

                //symtab ����
                if (line.label.length() > 0)
                    addSymbol(TokenList.indexOf(section), line);

                //littab �ӽ� ����
                if (line.operand.length > 0 && line.operand[0].startsWith("=")) {
                	Is_there_Literal = true;
                    String literal = line.operand[0];
                    literal = literal.replace("=C'", "");
                    literal = literal.replace("=X'", "");
                    literal = literal.replace("'", "");
                    if (section.literalTab.search(literal) == -1) {
                        if (line.operand[0].charAt(1) == 'X') {
                            section.literalTab.putName(literal, 1);
                        } else      //'C'
                            section.literalTab.putName(literal, 2);
                    }
                }
                //littab ����
                if (Is_there_Literal && (line.operator.equals("LTORG") || line.operator.equals("END"))) {
                	Is_there_Literal = false;
                    line.byteSize = addLiteral(TokenList.indexOf(section), LocationCounter);
                    LocationCounter += line.byteSize;
                }
                Location1 = LocationCounter;
            }
            //Section�� �ٲ� �� ù ���ο� Section�� ���� ����
            section.tokenList.get(0).location = LocationCounter;
        }

	}

    /**
     * ��ū�� ������ ��ū�� �����ϴ� �޸��� ũ�⸦ �����ϴ� �Լ� , �ּ� �˱� ���� ����
     * �Ű����� : �޸� ũ�⸦ ����� ��ū
     * ��ȯ: ��ū�� �����ϴ� �޸� ũ��
     */
	
    private int getLength(Token token) {
        int LocCnt= 0;
        //instTable�� �ִ� ��ɾ��� (�ּ� ���)
        String op = token.operator.replace("+", ""); //�Ű������� �ִ� +�� ����.
        
        if (instTable.instMap.get(op) != null)  //�ִٸ�
        {
        	LocCnt += instTable.instMap.get(op).format;
            if (token.operator.charAt(0) == '+') //ó���� +�� ��
            	LocCnt++;
        } 
        else if (token.operator.equals("RESW"))  //RESW�϶� 3* operand �� ��ŭ �ּ� ����
        	LocCnt += Integer.valueOf(token.operand[0]) * 3;
        
        else if (token.operator.equals("RESB"))//RESB�϶� 3* operand �� ��ŭ �ּ� ����
        	LocCnt += Integer.valueOf(token.operand[0]);
        else if (token.operator.equals("WORD"))
        	LocCnt += 3;
        else if (token.operator.equals("BYTE")) 
        {
            if (token.operand[0].charAt(0) == 'X')
            	LocCnt += (token.operand[0].length() - 3) / 2;
            else    //C�� ���
            	LocCnt += token.operand[0].length() - 3;
        }
        return LocCnt;
    }

    private void addSymbol(int section, Token line)
    {
        LabelTable symtab = TokenList.get(section).symTab;
        symtab.putName(line.label, 0);
        //EQU�̰�
        if (line.operator.equals("EQU")) {
            //-������ �ϸ�
            if (line.operand[0].contains("-")) {
                String[] operand = line.operand[0].split("-");
                int addr1 = symtab.search(operand[0]);
                int addr2 = symtab.search(operand[1]);
                if (addr1 != -1 && addr2 != -1) {
                    symtab.modifyName(line.label, addr1 - addr2);
                } else
                    symtab.modifyName(line.label, line.location);
                line.location = symtab.search(line.label);
            }
            //���� �ּ� ����(*)�̸�
            else if (line.operand[0].equals("*")) {
                symtab.modifyName(line.label, line.location);
            }
            //�����̸�
            
            else {
                int addr = symtab.search(line.operand[0]);
                //�ܺ� �����̸�
                if (addr == -1)
                    addr = 0;
                symtab.modifyName(line.label, addr);
            }
        } else
            symtab.modifyName(line.label, line.location);
    }

    /**
     * �ӽ� ����� LiteralTable�� ���ͷ��� �ּҸ� �Ҵ��Ѵ�.
     *
     * @param locctr : ���� �ּ�
     * @return : ���ͷ��� ������ �޸� ũ��
     */
    private int addLiteral(int section, int locctr) {
        int size = 0;
        LabelTable littab = TokenList.get(section).literalTab;
        //���� ���ǿ� �ӽ� ����Ǿ��ִ� ���ͷ��鿡�� �ּ� �Ҵ�
        for (String literal : littab.label) {
        	if(littab.search(literal)==1) //ó���� ���� ��. X�� ��
        	{
        		  littab.modifyName(literal, locctr + size);
                  size += literal.length() / 2;
                  littab.XorC.add('X');
                  break;
        	}
        	else if (littab.search(literal)==2) // C�� ��
        	{

                littab.modifyName(literal, locctr + size);
                size += literal.length();
                littab.XorC.add('C');
        	}
        }
        
        return size;
    }
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * 
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		  try {
	            File file = new File(fileName);
	            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
	            //SYMTAB ���
	            if (file.isFile() && file.canWrite()) {
	                for (LabelTable section : symtabList) {
	                    for (int i = 0; i < section.label.size(); i++)
	                        bufferedWriter.write(String.format("%-6s\t%X\n", section.label.get(i), section.locationList.get(i)));
	                    bufferedWriter.newLine();
	                }
	                bufferedWriter.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

	/**
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * 
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		 try {
	            File file = new File(fileName);
	            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
	            //LITTAB ���
	            if (file.isFile() && file.canWrite()) {
	                for (LabelTable section : literaltabList) {
	                    for (int i = 0; i < section.label.size(); i++)
	                        bufferedWriter.write(String.format("%-6s\t%X\n", section.label.get(i), section.locationList.get(i)));
	                    bufferedWriter.newLine();
	                }
	                bufferedWriter.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	}

	/**
	 * pass2 ������ �����Ѵ�.
	 * 
	 * 1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		  //�� �پ� �����鼭
        for (TokenTable section : TokenList) {
            for (int i = 0; i < section.tokenList.size(); i++)
                section.makeObjectCode(i);
        }
	}

	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * 
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		 //���� object code�� ����� codeList�� ����
        makeCodeList();
        //������� ���� object code ���
        try {
            File file = new File(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            if (file.isFile() && file.canWrite()) {
                for (String finalCode : codeList)
                    bufferedWriter.write(finalCode);
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	   private void makeCodeList() {
	        String objectCode = "";
	        int locctr, length, index;
	        //codeList�� ���� object code ����
	        for (TokenTable section : TokenList) {
	            for (int i = 0; i < section.tokenList.size(); i++) {
	                Token token = section.getToken(i);
	                switch (token.record) {
	                    case 'H':
	                    case 'D':
	                    case 'R':
	                    case 'M':
	                    case 'E':
	                        objectCode = String.format("%c%s\n", token.record, section.getObjectCode(i));
	                        break;
	                    case 'T':
	                        //�Է� ���� �̸� ���� ���
	                        locctr = token.location + token.byteSize;
	                        length = token.byteSize;
	                        for (index = i + 1; index < section.tokenList.size(); index++) {
	                            token = section.getToken(index);
	                            if (token.record == 0)
	                                continue;
	                            else if (token.record != 'T')        //T ���ڵ尡 �ƴϸ�
	                                break;
	                            if (token.location != locctr)        //���� ������������
	                                break;
	                            if (length + token.byteSize > 0x1E)  //���ѵ� ���̸� �ʰ��ϸ�
	                                break;
	                            locctr += token.byteSize;
	                            length += token.byteSize;
	                        }
	                        token = section.getToken(i);
	                        objectCode = String.format("%c%06X%02X", token.record, token.location, length);
	                        //T���ڵ� �Է�
	                        for (int j = i; j < index; j++)
	                            objectCode = objectCode.concat(String.format("%s", section.getObjectCode(j)));
	                        objectCode = objectCode.concat("\n");
	                        i = index - 1;
	                        break;
	                }
	                //codeList�� ����
	                this.codeList.add(objectCode);
	            }
	            this.codeList.add("\n");
	        }
	    }
}
