package userInput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import criticalSection.file.FileContent;
import criticalSection.file.FileContentInfo;

public class Editor {
    private static final Logger logger = LoggerFactory.getLogger(Editor.class);
    private File file = null;
    private ArrayList<String> lines = new ArrayList<>();
    private boolean active = false; 
    private ArrayList<String> notifications = new ArrayList<>();
    private File homeDir = null;

    public Editor(File homeDir) {
        this.homeDir = homeDir;
        logger.debug("Home dir for editor: " + homeDir.getPath());
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setFile(File file) {
        if (this.file != null) {
            logger.warn("Cannot edit a second file");
            System.out.println("Cannot edit a second file");
            return;
        }
        this.file = file;
        File newFile = getActualFile();
        lines.clear();
        if (!newFile.exists()) return;
        try {
            BufferedReader br = new BufferedReader(new FileReader(newFile));
            while (br.ready()) {
                lines.add(br.readLine());
            }
            br.close();
        } catch (IOException e) {
            logger.error("File not found", e);
        }
    }

    public void resetFile() {
        this.file = null;
    }

    public boolean isActive() {
        return active;
    }

    public void dump() {
        if (file == null) {
            return;
        }
        for (int i = 0; i < 50; i++) {
            System.out.println("~");
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ File Editor ~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(String.format("*** START OF FILE: %s ***", file.getName()));
        for (int i = 0; i < lines.size(); i++) {
            System.out.println((i+1) + "\t" + lines.get(i));
        }
        System.out.println(lines.size()+1);
        System.out.println("*** END OF FILE ***");
        System.out.println("Select Mode:\t\t/i insert [default]\t\t/r replace\t\t/d delete\t\t/c clear\t\t/e exit");
        System.out.println("followed by line number and text (ex. '/i 3 Hello World'): ");
        for (String notify : notifications) {
            System.out.println(notify);
        }
        System.out.print("                                          \r>");
        notifications.clear();
    }

    public FileContentInfo handleUserInput(String[] tokenStr) {
        ArrayList<String> tokens = new ArrayList<>(Arrays.asList(tokenStr));

        String token = null;
        String command = "/i";
        int fileSize = lines.size();
        int lineNum = fileSize;
        StringBuilder text = new StringBuilder();

        try {
            token = tokens.remove(0);
            if (token.charAt(0)=='/') {
                command = token;
                token = null;
                token = tokens.remove(0);
            }

            if (!command.equals("/i") || tokens.size() > 0) {
                try {
                    lineNum = Integer.parseInt(token);
                    token = null;
                    token = tokens.remove(0);
                } catch (Exception e) {
                    
                }
            }
        } catch (IndexOutOfBoundsException e) {
            
        }
        if (token!=null) text.append(token);
            
        for (String str : tokens) {
            text.append(" ").append(str);
        }

        logger.debug("command: " + command + " line#: " + lineNum + " text: " + text.toString());
        
        switch (command) {
            case "/e":
                logger.info("User terminated editor");
                setActive(false);
                FileContentInfo fcInfo = save();
                resetFile();
                
                return fcInfo;
            case "/r":
                if (lineNum <= fileSize && lineNum > 0) {
                    lines.set(lineNum - 1, text.toString());
                } else {
                    notifications.add("Invalid line number");
                }
                break;
            case "/d":
                if (lineNum <= fileSize && lineNum > 0) {
                    lines.remove(lineNum - 1);
                } else {
                    notifications.add("Invalid line number");
                }
                break;
            case "/c":
                lines.clear();
                break;
            default: // "/i insert"
            if (lineNum <= fileSize && lineNum >= 0) {
                    lineNum++;
                    lines.add(lineNum - 1, text.toString());
                } else {
                    notifications.add("Invalid line number");
                }
                break;
        }

        dump();

        return null;
    }

    public FileContentInfo save() {
        StringBuilder content = new StringBuilder();
        
        for (String line : lines) {
            content.append(line).append("\n");
        }

        String contentStr = content.toString();

        try {
            File newFile = getActualFile();
            PrintWriter pw = new PrintWriter(newFile);
            pw.write(contentStr);
            pw.close();

            logger.info("Editor writing to file: " + newFile.getPath());
        } catch (FileNotFoundException e) {
            logger.error("Could not write to file", e);
        }
        
        return new FileContentInfo(file.getPath(), new FileContent(contentStr, true));
    }

    public File getActualFile() {
        return new File(homeDir, file.getPath());
    }

}
