import java.util.ArrayList;
import java.util.Arrays;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�.
 * 
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ�
 * �̸� ��ũ��Ų��. section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit ������ �������� ���� ���� */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	LabelTable symTab;
	LabelTable literalTab;
	InstTable instTab;

	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;

	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * 
	 * @param symTab    : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param literaTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab   : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(LabelTable symTab, LabelTable literalTab, InstTable instTab) {
		this.symTab=symTab;
	    this.literalTab = literalTab;
        this.instTab = instTab;
        tokenList = new ArrayList<>();
	}

	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * 
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}

	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * 
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 �������� ����Ѵ�. instruction table, symbol table ���� �����Ͽ� objectcode�� �����ϰ�, �̸�
	 * �����Ѵ�.
	 * 
	 * @param index
	 */

    public void makeObjectCode(int index){
        Token token = this.getToken(index);
        Instruction inst = this.instTab.instMap.get(token.operator.replace("+", ""));
        //START�� CSECT�϶� H 
        if(token.operator.equals("START") || token.operator.equals("CSECT")) {
            token.record = 'H';
            token.objectCode = String.format("%-6s%06X%06X", token.label, 0, token.location);
        }
        //EXTDEF
        else if(token.operator.equals("EXTDEF")) {
            token.record = 'D';
            for(String extdef : token.operand)
                token.objectCode = token.objectCode.concat(String.format("%-6s%06X", extdef, this.symTab.search(extdef)));
        }
        //EXTREF
        else if(token.operator.equals("EXTREF")) {
            token.record = 'R';
            for(String extref : token.operand)
                token.objectCode = token.objectCode.concat(String.format("%-6s", extref));
        }
        //�Ϲ� ��ɾ�
        else if(inst != null) {
            token.record = 'T';
            //nixbpe��Ʈ�� ä��� displacement ���ϱ�
            int displacement = setNixbpe(token, inst);
            //format�� ���� objectCode ����
            switch(token.byteSize) {
                case 2:
                    token.objectCode = String.format("%04X", ((inst.opcode<<4 | token.nixbpe) << 4) | displacement);
                    break;
                case 3:
                    token.objectCode = String.format("%06X", ((inst.opcode<<4 | token.nixbpe) << 12) | (displacement & 0xFFF));
                    break;
                case 4:
                    token.objectCode = String.format("%08X", ((inst.opcode<<4 | token.nixbpe) << 20) | (displacement & 0xFFFFF));
                    break;
            }
        }
        //LTORG�� END
        else if(token.operator.equals("LTORG") || token.operator.equals("END")) {
            token.record = 'T';
            LabelTable littab = this.literalTab;
            for(int i=0; i<littab.label.size(); i++) {
                String literal = littab.label.get(i);
                int value = 0;
                switch(littab.XorC.get(i)) {
                    case 'X':
                        value = Integer.parseInt(literal, 16);
                        break;
                    case 'C':
                        for(int j=0; j<literal.length(); j++) {
                            value = value << 8;
                            value |= literal.charAt(j);
                        }
                        break;
                }
                token.objectCode = token.objectCode.concat(String.format("%0"+token.byteSize*2+"X", value));
            }
        }
        //BYTE
        else if(token.operator.equals("BYTE")) {
            token.record = 'T';
            String value = token.operand[0].replace("'", "");
            int intValue = 0;
            //X�̸�
            if(value.startsWith("X"))
                intValue = Integer.parseInt(value.replace("X", ""), 16);
            //C�̸�
            else {
                value = value.replace("C", "");
                for(int i=0; i<value.length(); i++) {
                    intValue = intValue << 8;
                    intValue |= value.charAt(i);
                }
            }
            token.objectCode = String.format("%0"+token.byteSize*2+"X", intValue);
        }
        //WORD
        else if(token.operator.equals("WORD")) {
            token.record = 'T';
            int value;
            //���� �����̸�(-)
            if(token.operand[0].contains("-")) {
                String[] operand = token.operand[0].split("-");
                int addr1 = this.symTab.search(operand[0]);
                int addr2 = this.symTab.search(operand[1]);
                if(addr1 != -1 && addr2 != -1)
                    value = addr2 - addr1;
                //�ܺ� �����̸� M���ڵ� �߰�
                else {
                    Token mToken = new Token('M', String.format("%06X06+%s", token.location, operand[0]));
                    this.tokenList.add(mToken);
                    mToken = new Token('M', String.format("%06X06-%s", token.location, operand[1]));
                    this.tokenList.add(mToken);
                    value = 0;
                }
            }
            //�����̸�
            else {
                int addr = symTab.search(token.operand[0]);
                //�ܺ� �����̸� M���ڵ� �߰�
                if(addr == -1) {
                    value = 0;
                    Token mToken = new Token('M', String.format("%06X06+%s", token.location, token.operand[0]));
                    this.tokenList.add(mToken);
                }
                else
                    value = addr;
            }
            token.objectCode = String.format("%0"+token.byteSize*2+"X", value);
        }
        //section�� �������̸� E record �߰�
        if(token.record != 'E' && index == this.tokenList.size() - 1) {
            if(this.getToken((0)).operator.equals("START")) {
                Token eToken = new Token('E', String.format("%06X", 0));
                this.tokenList.add(eToken);
            }
            else {
                Token eToken = new Token('E', "");
                this.tokenList.add(eToken);
            }
        }
    }

    /**
     * �־��� ��ū ������ ��ɾ��� ��� nixbpe�� �����ϰ� displacement�� �����Ѵ�
     * @param token : nixbpe�� ������ ��ū
     * @return : displacement
     */
    private int setNixbpe(Token token, Instruction inst) {
        int displacement = 0;
        //ni��Ʈ
        //2byte format�̸�(�������� ����)
        if(inst.format == 2) {
            //n = 0, i = 0
            token.setFlag(nFlag, 0);
            token.setFlag(iFlag, 0);
        }
        //3~4byte format�̸�
        else {
            //n = 1, i = 1���� �ʱ�ȭ
            token.setFlag(nFlag, 1);
            token.setFlag(iFlag, 1);
            if(inst.Num_operand > 0)
            {
                //immediate addressing�̸� n = 0
                if(token.operand[0].startsWith("#"))
                    token.setFlag(nFlag, 0);
                    //indirect addressing�̸� i = 0
                else if(token.operand[0].startsWith("@"))
                    token.setFlag(iFlag, 0);
            }
        }
        //x��Ʈ
        if(token.operand.length > 1 && token.operand[1].equals("X"))
            token.setFlag(xFlag, 1);
        else
            token.setFlag(xFlag, 0);
        //bp��Ʈ
        //2byte format�̸�(�������� ����)
        if(token.byteSize == 2)
        {
            token.setFlag(bFlag, 0);
            token.setFlag(pFlag, 0);
            ArrayList<String> rList = new ArrayList<>(Arrays.asList("A", "X", "L", "B", "S", "T", "F", "", "PC", "SW"));
            switch(token.operand.length) {
                case 2:
                    displacement = rList.indexOf(token.operand[1]);
                case 1:
                    displacement |= rList.indexOf(token.operand[0]) << 4;
                    break;
            }
        }
        else 
        {
            //immediate addressing �̸� b = 0, p = 0
            if(token.getFlag(nFlag | iFlag) == iFlag) {
                token.setFlag(bFlag, 0);
                token.setFlag(pFlag, 0);
                displacement = Integer.parseInt(token.operand[0].replace("#", ""));
            }
            //�Ϲ����� 3-4byte format
            else {
                token.setFlag(bFlag, 0);
                token.setFlag(pFlag, 1);
                //symtab, littab���� �˻�
                int target = this.symTab.search(token.operand[0].replace("@", ""));
                //symtab�� ������ displacement�� ����Ͽ� b,p��Ʈ �Է�
                if(target != -1) {
                    if (Math.abs(target - (token.location + token.byteSize)) <= 0x7FF)
                        displacement = target - (token.location + token.byteSize);
                    else {
                        token.setFlag(bFlag, 1);
                        token.setFlag(pFlag, 0);
                    }
                }
                //littab�� ������ displacement�� ����Ͽ� b,p��Ʈ �Է�
                else {
                    String literal = token.operand[0];
                    literal = literal.replace("=C'", "");
                    literal = literal.replace("=X'", "");
                    literal = literal.replace("'", "");
                    target = this.literalTab.search(literal);
                    if(target != -1) {
                        if (Math.abs(target - (token.location + token.byteSize)) <= 0x7FF)
                            displacement = target - (token.location + token.byteSize);
                        else {
                            token.setFlag(bFlag, 1);
                            token.setFlag(pFlag, 0);
                        }
                    }
                    else {
                        token.setFlag(pFlag, 0);
                        if(literal.length() > 0) {
                            Token mToken = new Token('M', String.format("%06X05+%s", token.location + 1, literal));
                            this.tokenList.add(mToken);
                        }
                    }
                }
            }
        }
        //e��Ʈ
        if(token.byteSize == 4)
            token.setFlag(eFlag, 1);
        else
            token.setFlag(eFlag, 0);

        return displacement;
    }


	/**
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ �� �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. �ǹ� �ؼ��� ������ pass2����
 * object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token {
	// �ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������
	String objectCode;
	int byteSize;
	
	 char record;    //��ū�� ���ڵ� ����(H, D, R, T, M, E)
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�.
	 * 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	 public Token(String line) {
	        //�ʱ�ȭ
	        label = "";
	        operator = "";
	        operand = new String[0];
	        comment = "";
	        objectCode = "";
	        parsing(line);
	    }
	/**
     * M, E���ڵ带 �߰��ϴ� ����� ������
     *
     * @param record     M �Ǵ� E ���ڵ�
     * @param objectCode ������ objectCode
     */
	 public Token(char record, String objectCode) {
	        this.record = record;
	        this.objectCode = objectCode;
	        label = "";
	        operator = "";
	        operand = new String[0];
	        comment = "";
	    }

	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * 
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	 public void parsing(String line) {
	        //�и�
	        String[] info = line.split("\t");
	        //�Է�
	        switch (info.length) {
	            case 4:
	                comment = info[3];
	            case 3:
	                operand = info[2].split(",");
	            case 2:
	                operator = info[1];
	            case 1:
	                label = info[0];
	                break;
	        }
	    }

	/**
	 * n,i,x,b,p,e flag�� �����Ѵ�.
	 * 
	 * 
	 * ��� �� : setFlag(nFlag, 1) �Ǵ� setFlag(TokenTable.nFlag, 1)
	 * 
	 * @param flag  : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {

        // ��Ʈ�� ä���
        nixbpe |= flag;
        //value�� 0�̸� ����.
        if (value == 0)
            nixbpe -= flag;
	}

	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ�.
	 * 
	 * ��� �� : getFlag(nFlag) �Ǵ� getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */

	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
