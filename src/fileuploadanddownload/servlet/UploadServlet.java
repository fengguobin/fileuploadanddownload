package fileuploadanddownload.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//定义可上传文件类型
	private String Ext_Name = "jpg,txt,doc";
       
    

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		//设置上传文件的保存目录 workspace
		String savePath = this.getServletContext().getRealPath("WEB-INF/upload");
		File saveFileDir = new File(savePath);
		if(!saveFileDir.exists()){
			saveFileDir.mkdirs();
		}
		//设置上传文件的临时保存目录
		String tmpPath = this.getServletContext().getRealPath("WEB-INF/tmp");
		File tmpFileDir = new File(tmpPath);
		if(!tmpFileDir.exists()){
			tmpFileDir.mkdirs();
		}
		
		//消息提醒
		String message = "";
		try{
			//1、创建工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(1024*10);  //设置缓冲区大小，当文件超过缓冲区大小时，生成临时文件放到tmp目录中
			factory.setRepository(tmpFileDir);  //设置缓冲区目录
			//2、创建文件上传解析器
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setProgressListener(new ProgressListener(){     //设置文件监听器，监听文件上传进度
				long num = 0;
				public void update(long readedBytes , long totalBytes, int currentItem) {
					// TODO Auto-generated method stub
					//System.out.println("当前已处理：" + readedBytes  + "-----------文件大小为：" + totalBytes + "--" + currentItem);
				
					long progress = readedBytes*100/totalBytes;
					if(progress==num)
						return;
					num = progress;
					System.out.println("上传进度:" + progress + "%");
				}   	
			});  
			upload.setHeaderEncoding("utf-8");     //设置编码格式
			//3、判断提交上来的数据是否来自表单
			if(!ServletFileUpload.isMultipartContent(request)){return;} //按传统方式获取数据
			upload.setFileSizeMax(1024*1024*1);  //设置上传单个文件最大值为1M
			upload.setSizeMax(1024*1024*10);    //设置上传总文件大小为10M
			List items = upload.parseRequest(request);
			Iterator itr = items.iterator();
			while(itr.hasNext()){
				FileItem item = (FileItem)itr.next();
				if(item.isFormField()){    //item中装入的是普通输入数据
					String desc = item.getFieldName();
					String value = item.getString("utf-8");
					System.out.println(desc + " = " + value);
				}else{                      //item中装入的是上传文件
					String fileName = item.getName();
					System.out.println("文件名:" + fileName);
					if(fileName == null && fileName.trim().length() == 0){ continue;}
					fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);    //获取文件名，不同浏览器提交的文件名不同，只保留文件名
					System.out.println("处理后的文件名:" + fileName);
					String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();  //获取文件扩展名，即文件类型
					System.out.println("文件扩展名:" + fileExt);
					if(!Ext_Name.contains(fileExt)){
						System.out.println("上传文件扩展名是不允许的扩展名：" + fileExt);
						message = message + "文件：" + fileName + "，上传文件扩展名是不允许的扩展名：" + fileExt + "<br/>";
						break;
					}
					if(item.getSize() == 0){ continue ;}
					if(item.getSize() > 1024*1024.1){
						System.out.println("上传文件大小:" + item.getSize());
						message = message + "文件：" + fileName + "，上传文件大小超过限制大小：" + upload.getFileSizeMax() + "<br/>";
						break;
					}
					
					
					//把文件写到指定的文件夹中
					String saveFileName = makeFileName(fileName);  //保存文件名
					InputStream is = item.getInputStream();
					FileOutputStream out = new FileOutputStream(savePath + "\\" + saveFileName );
					byte buffer[] = new byte[1024];
					int len = 0;
					while((len = is.read(buffer)) > 0 ){
						out.write(buffer,0,len);
					}
					out.close();
					is.close();
					item.delete();
					 message = message + "文件：" + fileName + "，上传成功<br/>";
				}
			}
		}catch(FileSizeLimitExceededException  e){
			message = message + "上传文件大小超过限制<br/>";
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}finally {
			request.setAttribute("message", message);
			request.getRequestDispatcher("/message.jsp").forward(request, response);
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doGet(request, response);
	}
	private String makeFileName(String fileName){
		//防止文件覆盖现象发生，为上传文件产生一个唯一文件名
		return UUID.randomUUID().toString().replaceAll("-", "") + "_" +fileName;
	}

}
