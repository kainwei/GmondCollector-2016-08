<%@ page contentType="text/html; charset=GBK" %>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<html><head><title>upFile</title></head>
<body bgcolor="#ffffff">
<%
    //���������ļ�������ֽ�
    int MAX_SIZE = 102400 * 102400;
// ������·���ı������
    String rootPath;
//�����ļ�������
    DataInputStream in = null;
    FileOutputStream fileOut = null;
//ȡ�ÿͻ��˵������ַ
    String remoteAddr = request.getRemoteAddr();
//��÷�����������
    String serverName = request.getServerName();

//ȡ�û���������ľ��Ե�ַ
    String realPath = request.getRealPath(serverName);
    realPath = realPath.substring(0,realPath.lastIndexOf("\\"));
//�����ļ��ı���Ŀ¼
    rootPath = realPath + "\\upload\\";
//ȡ�ÿͻ����ϴ�����������
    String contentType = request.getContentType();
    try{
        if(contentType.indexOf("multipart/form-data") >= 0){
//�����ϴ�������
            in = new DataInputStream(request.getInputStream());
            int formDataLength = request.getContentLength();
            if(formDataLength > MAX_SIZE){
                out.println("<P>�ϴ����ļ��ֽ��������Գ���" + MAX_SIZE + "</p>");
                return;
            }
//�����ϴ��ļ�������
            byte dataBytes[] = new byte[formDataLength];
            int byteRead = 0;
            int totalBytesRead = 0;
//�ϴ������ݱ�����byte����
            while(totalBytesRead < formDataLength){
                byteRead = in.read(dataBytes,totalBytesRead,formDataLength);
                totalBytesRead += byteRead;
            }
//����byte���鴴���ַ���
            String file = new String(dataBytes);
//out.println(file);
//ȡ���ϴ������ݵ��ļ���
            String saveFile = file.substring(file.indexOf("filename=\"") + 10);
            saveFile = saveFile.substring(0,saveFile.indexOf("\n"));
            saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1,saveFile.indexOf("\""));
            int lastIndex = contentType.lastIndexOf("=");
//ȡ�����ݵķָ��ַ���
            String boundary = contentType.substring(lastIndex + 1,contentType.length());
//��������·�����ļ���
            String fileName = rootPath + saveFile;
//out.print(fileName);
            int pos;
            pos = file.indexOf("filename=\"");
            pos = file.indexOf("\n",pos) + 1;
            pos = file.indexOf("\n",pos) + 1;
            pos = file.indexOf("\n",pos) + 1;
            int boundaryLocation = file.indexOf(boundary,pos) - 4;
//out.println(boundaryLocation);
//ȡ���ļ����ݵĿ�ʼ��λ��
            int startPos = ((file.substring(0,pos)).getBytes()).length;
//out.println(startPos);
//ȡ���ļ����ݵĽ�����λ��
            int endPos = ((file.substring(0,boundaryLocation)).getBytes()).length;
//out.println(endPos);
//��������ļ��Ƿ����
            File checkFile = new File(fileName);
            if(checkFile.exists()){
                out.println("<p>" + saveFile + "�ļ��Ѿ�����.</p>");
            }
//��������ļ���Ŀ¼�Ƿ����
            File fileDir = new File(rootPath);
            if(!fileDir.exists()){
                fileDir.mkdirs();
            }
//�����ļ���д����
            fileOut = new FileOutputStream(fileName);
//�����ļ�������
            fileOut.write(dataBytes,startPos,(endPos - startPos));
            fileOut.close();
            out.println(saveFile + "�ļ��ɹ�����.</p>");
        }else{
            String content = request.getContentType();
            out.println("<p>�ϴ����������Ͳ���multipart/form-data</p>");
        }
    }catch(Exception ex){
        throw new ServletException(ex.getMessage());
    }
%>
</body>
</html>