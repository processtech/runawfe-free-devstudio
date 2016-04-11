package ru.runa.gpd.formeditor.wysiwyg;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.runa.gpd.EditorsPlugin;

/**
 * Servlet to upload and browse files.<br>
 * 
 * This servlet accepts 4 commands used to retrieve and create files and folders from a server directory. The allowed commands are:
 * <ul>
 * <li>GetFolders: Retrive the list of directory under the current folder
 * <li>GetFoldersAndFiles: Retrive the list of files and directory under the current folder
 * <li>CreateFolder: Create a new directory under the current folder
 * <li>FileUpload: Send a new file to the server (must be sent with a POST)
 * </ul>
 * 
 */
public class ConnectorServlet extends HttpServlet {
    private static final long serialVersionUID = -4646937668204517913L;
    private static boolean debug = true;

    /**
     * Initialize the servlet.<br>
     */
    @Override
    public void init() throws ServletException {
        String debugStr = getInitParameter("debug");
        if (debugStr != null) {
            debug = new Boolean(debugStr).booleanValue();
        }
    }

    /**
     * Manage the Get requests (GetFolders, GetFoldersAndFiles, CreateFolder).<br>
     * 
     * The servlet accepts commands sent in the following format:<br>
     * connector?Command=CommandName&Type=ResourceType&CurrentFolder=FolderPath<br>
     * <br>
     * It execute the command and then return the results to the client in XML format.
     * 
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/xml; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        String commandStr = request.getParameter("Command");
        String typeStr = request.getParameter("Type");
        if (typeStr == null) {
            typeStr = "File";
        }
        String currentFolderStr = request.getParameter("CurrentFolder");
        String currentDirPath = ConnectorServletHelper.getBaseDir() + currentFolderStr;
        File currentDir = new File(currentDirPath);
        if (!currentDir.exists()) {
            currentDir.mkdir();
        }
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }
        Node root = createCommonXml(document, commandStr, typeStr, currentFolderStr, /* request.getContextPath() + currentDirPath* currentFolderStr */
                "");
        if (debug) {
            EditorsPlugin.logInfo("Command = " + commandStr);
        }
        if (commandStr.equals("GetFolders")) {
            getFolders(currentDir, root, document);
        } else if (commandStr.equals("GetFoldersAndFiles")) {
            getFolders(currentDir, root, document);
            getFiles(currentDir, root, document);
        } else if (commandStr.equals("CreateFolder")) {
            String newFolderStr = request.getParameter("NewFolderName");
            File newFolder = new File(currentDir, newFolderStr);
            String retValue = "110";
            if (newFolder.exists()) {
                retValue = "101";
            } else {
                try {
                    boolean dirCreated = newFolder.mkdir();
                    if (dirCreated) {
                        retValue = "0";
                    } else {
                        retValue = "102";
                    }
                } catch (SecurityException sex) {
                    retValue = "103";
                }
            }
            setCreateFolderResponse(retValue, root, document);
        }
        document.getDocumentElement().normalize();
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            if (debug) {
                StreamResult dbgResult = new StreamResult(System.out);
                transformer.transform(source, dbgResult);
            }
        } catch (Exception ex) {
            EditorsPlugin.logError(ex.getMessage(), ex);
        }
        out.flush();
        out.close();
    }

    /**
     * Manage the Post requests (FileUpload).<br>
     * 
     * The servlet accepts commands sent in the following format:<br>
     * connector?Type=ResourceType&CurrentFolder=FolderPath<br>
     * <br>
     * It store the file (renaming it in case a file with the same name exists) and then return an HTML file with a javascript command in it.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (debug) {
            EditorsPlugin.logInfo("--- Starting FileUpload ---");
        }
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        String currentFolderStr = request.getParameter("CurrentFolder");
        if (currentFolderStr == null) {
            currentFolderStr = "/";
        }
        String typeStr = request.getParameter("Type");
        if (typeStr == null) {
            typeStr = "File";
        }
        String currentDirPath = ConnectorServletHelper.getBaseDir() + currentFolderStr;
        if (debug) {
            EditorsPlugin.logInfo("currentDirPath = " + currentDirPath + ", currentFolderStr = " + currentFolderStr);
        }
        String retVal = "0";
        String fileUrl = "";
        String errorMessage = "";
        DiskFileUpload upload = new DiskFileUpload();
        upload.setHeaderEncoding("UTF-8");
        try {
            List<FileItem> items = upload.parseRequest(request);
            // We expect first item being 'NewFile'
            FileItem uplFile = items.get(0);
            String fileNameLong = uplFile.getName().replace('\\', '/');
            String[] pathParts = fileNameLong.split("/");
            String fileName = pathParts[pathParts.length - 1];
            File pathToSave = new File(currentDirPath, fileName);
            // removing '/'
            fileUrl = (currentFolderStr + fileName).substring(1);
            if (pathToSave.exists()) {
                retVal = "201";
            }
            uplFile.write(pathToSave);
            // sync after upload
            ConnectorServletHelper.sync();
        } catch (Exception ex) {
            EditorsPlugin.logError("Upload file", ex);
            retVal = "1";
            errorMessage = ex.getMessage();
        }
        out.println("<script type=\"text/javascript\">");
        out.println("if (window.parent.OnUploadCompleted) { ");
        out.println("	window.parent.OnUploadCompleted(" + retVal + ",'" + fileUrl + "','" + errorMessage + "');");
        out.println("} else {");
        out.println("	window.parent.frames['frmUpload'].OnUploadCompleted(" + retVal + ",'" + fileUrl + "','" + errorMessage + "');");
        out.println("}");
        out.println("</script>");
        out.flush();
        out.close();
        if (debug) {
            EditorsPlugin.logInfo("--- End FileUpload ---");
        }
    }

    private void setCreateFolderResponse(String retValue, Node root, Document doc) {
        Element myEl = doc.createElement("Error");
        myEl.setAttribute("number", retValue);
        root.appendChild(myEl);
    }

    private void getFolders(File dir, Node root, Document doc) {
        Element folders = doc.createElement("Folders");
        root.appendChild(folders);
        File[] fileList = dir.listFiles();
        for (int i = 0; i < fileList.length; ++i) {
            if (fileList[i].isDirectory()) {
                Element myEl = doc.createElement("Folder");
                myEl.setAttribute("name", fileList[i].getName());
                folders.appendChild(myEl);
            }
        }
    }

    private void getFiles(File dir, Node root, Document doc) {
        Element files = doc.createElement("Files");
        root.appendChild(files);
        File[] fileList = dir.listFiles(new ConnectorServletHelper.FileExtensionFilter());
        for (int i = 0; i < fileList.length; ++i) {
            if (fileList[i].isFile()) {
                Element myEl = doc.createElement("File");
                myEl.setAttribute("name", fileList[i].getName());
                myEl.setAttribute("size", "" + fileList[i].length() / 1024);
                files.appendChild(myEl);
            }
        }
    }

    private Node createCommonXml(Document doc, String commandStr, String typeStr, String currentPath, String currentUrl) {
        Element root = doc.createElement("Connector");
        doc.appendChild(root);
        root.setAttribute("command", commandStr);
        root.setAttribute("resourceType", typeStr);
        Element myEl = doc.createElement("CurrentFolder");
        myEl.setAttribute("path", currentPath);
        myEl.setAttribute("url", currentUrl);
        root.appendChild(myEl);
        return root;
    }
}
