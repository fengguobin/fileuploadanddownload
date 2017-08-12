package fileuploadanddownload.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ListFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		//获取上传文件的目录
		 String uploadFilePath = this.getServletContext().getRealPath("WEB-INF/upload");
		//存储要下载的文件名
		 Map<String,String> fileNameMap = new HashMap<String,String>();
		//递归遍历uploadFilePath下的所有文件
		 listFile(new File(uploadFilePath),fileNameMap);
		 request.setAttribute("fileNameMap", fileNameMap);
		 request.getRequestDispatcher("/listFile.jsp").forward(request, response);
	}



	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void listFile(File file, Map<String, String> map) {
		//file不是文件是目录
		if(!file.isFile()){
			File files[] = file.listFiles();
			if(files == null) return;
			for(File f : files ){
				listFile(f,map);
			}
		}else{
			String realName = file.getName().substring(file.getName().indexOf("_") + 1);
			// file.getName()得到的是文件的原始名称，这个名称是唯一的，因此可以作为key，realName是处理过后的名称，有可能会重复
			map.put(file.getName(), realName);
		}	
	}
}
