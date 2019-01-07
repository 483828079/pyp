package com.pinyougou.shop.controller;

import com.pinyougou.common.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
	/*如果是对象使用@Autowired注入
	 * 如果是基本类型或者String
	 * 因为是配置文件所以肯定读取的是String
	 * 所以可以将String转换为其他类型，但是必须是这个类型的字符串形式
	 * */
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;//文件服务器地址

	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		//1、取文件的扩展名
		String originalFilename = file.getOriginalFilename();
		String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		try {
			//2、创建一个 FastDFS 的客户端
			//其实就是通过配置文件创建了storageClient对象
			//至于路径读取的是classpath，会截取掉classpath:
			//替换成当前classpath+/，+ classPath下的配置，也就是磁盘位置的配置文件
			FastDFSClient fastDFSClient
					= new FastDFSClient("classpath:config/fdfs_client.conf");
			//3、执行上传处理
			// 有两种方式来指定上传的文件，一种是通过文件在磁盘的路径
			// 一种直接就是通过文件的字节码。
			// 因为是将上传的文件写到某个位置，所以肯定能获取该文件的字节码。
			// 返回值就是组名+文件路径，所以只需要ip就能把刚上传的图片显示在浏览器上
			String path = fastDFSClient.uploadFile(file.getBytes(), extName);
			//4、拼接返回的 url 和 ip 地址，拼装成完整的 url
			String url = FILE_SERVER_URL + path;
			// 如果上传成功返回能够显示图片的url
			return new Result(true, url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "上传失败");
		}
	}
}
