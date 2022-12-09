package settings;

import clock.ClockType;

public class Settings {
    public final static ClockType CLOCK_TYPE = ClockType.LAMPORT;
    public final static EditorType EDITOR_TYPE = EditorType.VSCODE;
    public final static SystemType SYSTEM_TYPE = SystemType.DISTRIBUTED;
    public final static String SERVER_COORD_IP_ADDR = "192.168.0.148";
    public final static int SERVER_COORD_PORT_NUM = 12345;
    public final static int SERVER_COORD_ID = 0;
    public final static int SERVER_PROCESS_ID = 0;
    public final static int LOCAL_PORT_NUM = 12345;
    public final static int MAX_CLIENTS = 10;
}
