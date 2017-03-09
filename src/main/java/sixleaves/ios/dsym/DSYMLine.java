package sixleaves.ios.dsym;

/**
 * Created by sixleaves on 2017/3/9.
 */
public class DSYMLine {

    public String frameNumber;
    public String libName;
    public String funcAddress;
    public String fileAddress;
    public String codeLineOffset;

    // 和funcAddress的映射
    public String funcName;



    public static void main(String...args) {

        System.out.println("Hello SweetPeng!");


    }
}
