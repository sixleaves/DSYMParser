package sixleaves.ios.dsym;

import java.io.*;

/**
 * Created by sixleaves on 2017/3/9.
 */
public class DSYMParser {
    public static final String CPU_TYPE_ARM64="arm64";
    public static final String CPU_TYPE_ARM32="armv7";
    private String _dsymPath;
    private String _cpuType;
    private String _appPath;
    private String _appName;
    private static final DSYMParser parser = new DSYMParser();

    private DSYMParser() {}

    public static DSYMParser getInstance() {
        return parser;
    }

    public String getDsymPath() {
        return _dsymPath;
    }

    public void setDsymPath(String dsymPath) {
        _dsymPath = dsymPath;
    }

    public String getCpuType() {
        return _cpuType;
    }

    public void setCpuType(String cpuType) {
        _cpuType = cpuType;
    }

    public String getAppPath() {
        return _appPath;
    }

    public void setAppPath(String appPath) {
        char[] temp = appPath.toCharArray();
        if (temp[temp.length - 1] == '/') {
            appPath = appPath.substring(0, appPath.length() - 1);
        }

        setAppName(appPath);

        _appPath = appPath;
    }

    public String getAppName() {
        return _appName;
    }

    public void setAppName(String appName) {

        int first = appName.lastIndexOf("/");
        int end = appName.lastIndexOf(".");
        if (first != -1)
            _appName = appName.substring(first + 1, end);
        else
            _appName = appName;
    }

    // dwarfdump --lookup 0x00000001000af218 --arch arm64 nzlm.app.dSYM
    /*
    *
    * Not Found
----------------------------------------------------------------------
 File: nzlm.app.dSYM/Contents/Resources/DWARF/nzlm (arm64)
----------------------------------------------------------------------
Looking up address: 0x00000001839704e8 in .debug_info... not found.
Looking up address: 0x00000001839704e8 in .debug_frame... not found.


// Found
----------------------------------------------------------------------
 File: nzlm.app.dSYM/Contents/Resources/DWARF/nzlm (arm64)
----------------------------------------------------------------------
Looking up address: 0x00000001000af218 in .debug_info... found!

0x00106050: Compile Unit: length = 0x00001e0c  version = 0x0002  abbr_offset = 0x00000000  addr_size = 0x08  (next CU at 0x00107e60)

0x0010605b: TAG_compile_unit [110] *
             AT_producer( "Apple LLVM version 8.0.0 (clang-800.0.38)" )
             AT_language( DW_LANG_ObjC )
             AT_name( "/Volumes/disk2/jenkins/app/workspace/fac-iOS_nzlm/Pods/MJRefresh/MJRefresh/Base/MJRefreshBackFooter.m" )
             AT_stmt_list( 0x00054b9f )
             AT_comp_dir( "/Volumes/disk2/jenkins/app/workspace/fac-iOS_nzlm/Pods" )
             AT_APPLE_optimized( 0x01 )
             AT_APPLE_major_runtime_vers( 0x02 )
             AT_low_pc( 0x00000001000ae918 )
             AT_high_pc( 0x00000001000af4fc )

0x0010770b:     TAG_subprogram [126] *
                 AT_low_pc( 0x00000001000af120 )
                 AT_high_pc( 0x00000001000af268 )
                 AT_frame_base( reg29 )
                 AT_name( "__32-[MJRefreshBackFooter setState:]_block_invoke.62" )
                 AT_decl_file( "/Volumes/disk2/jenkins/app/workspace/fac-iOS_nzlm/Pods/MJRefresh/MJRefresh/Base/MJRefreshBackFooter.m" )
                 AT_decl_line( 114 )
                 AT_prototyped( 0x01 )
                 AT_APPLE_optimized( 0x01 )

0x00107732:         TAG_lexical_block [117] *
                     AT_low_pc( 0x00000001000af13c )
                     AT_high_pc( 0x00000001000af268 )
Line table dir : '/Volumes/disk2/jenkins/app/workspace/fac-iOS_nzlm/Pods/MJRefresh/MJRefresh/Base'
Line table file: 'MJRefreshBackFooter.m' line 122, column 43 with start address 0x00000001000af218

Looking up address: 0x00000001000af218 in .debug_frame... not found.
    *
    * */

    // atos -arch arm64 -o nzlm.app/nzlm -l 0x183934000 0x00000001839704e8
    public DSYMLine symbolificate(String frameNum, String libName, String funcAdd, String fileAdd, String codeLineOffset) throws IOException {

        DSYMLine parseredLine = new DSYMLine();
        parseredLine.frameNumber = frameNum;
        parseredLine.libName = libName;
        parseredLine.funcAddress = funcAdd;
        parseredLine.fileAddress = fileAdd;
        parseredLine.codeLineOffset = codeLineOffset;

        String dwarfdumpCmd = String.format("dwarfdump --lookup %s --arch %s %s", funcAdd, getCpuType(), getDsymPath());
        String atosCmd = String.format("atos -arch %s -o %s -l %s %s", getCpuType(), getAppPath() + "/" + getAppName(), fileAdd, funcAdd);

        Process process = Runtime.getRuntime().exec(dwarfdumpCmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        int counter = 0;
        while ((line = reader.readLine())!= null) {
            System.out.println(line);
            counter++;
            if (line.contains("AT_name")) {

                int s = line.indexOf("[");
                int e = line.indexOf("]");
                if (-1 != s && -1 != e)
                    parseredLine.funcName = line.substring(s, e + 1);
                else
                    parseredLine.funcName = line;
            }
        }

        if (counter <= 6) {
            process = Runtime.getRuntime().exec(atosCmd);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = null;
            counter = 0;
            while ((line = reader.readLine())!= null) {
                parseredLine.funcName = line;
                counter++;
            }
        }
        return parseredLine;
    }

    public static void main(String...args) throws IOException {

        DSYMParser parser = DSYMParser.getInstance();
        parser.setDsymPath("/Users/sixleaves/Desktop/nzlm.app.dSYM");
        parser.setAppPath("/Users/sixleaves/Desktop/nzlm.app/");
        parser.setCpuType(DSYMParser.CPU_TYPE_ARM64);
        DSYMLine line = parser.symbolificate("0", "UIKit", "0x00000001000af218", "0x183934000", "247016");
        System.out.println(line);
    }



}
