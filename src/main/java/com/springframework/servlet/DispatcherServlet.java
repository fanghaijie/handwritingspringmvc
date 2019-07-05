package com.springframework.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springframework.annotation.MyAutowired;
import com.springframework.annotation.MyController;
import com.springframework.annotation.MyRepository;
import com.springframework.annotation.MyRequestMapping;
import com.springframework.annotation.MyService;

public class DispatcherServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//跟web.xml中的param-name的值一样
	private static final String LOCATION = "contextConfigLocation";
	
	//保存所有的配置信息
	private Properties p = new Properties();
	
	//保存所有被扫描到的相关的类名
	private List<String> classNames= new ArrayList<>();

	//核心IOC容器，保存所有初始化的Bean
	private Map<String,Object> ioc = new HashMap<>();
	
	//保存所有的url和方法的映射关系
	private Map<String,Method> handlerMapping = new HashMap<>();
	
	public DispatcherServlet() {
		//构造函数中的初始化工作只会在容器构造这个servlet时做一次。servlet的实例是会被多个请求复用，但是构造函数却只能提供一次初始化，所以必须将初始化工作放入init中，由容器来控制。
		super();
		System.out.println("DispatcherServlet构造方法");
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 当Servlet容器启动时，会调用DispatcherServlet的init()方法，从init方法的参数中，
	 * 我们可以拿到主配置文件的路径，从能够读取到配置文件中的信息。
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("init方法");
		//1.加载配置文件
		doLoadCongig(config.getInitParameter(LOCATION));
		//2.扫描所有相关的类
		doScanner(p.getProperty("scanPackage"));
		//3.初始化所有的相关类的实例，并保存到IOC容器中
		doInstance();
		//4.依赖注入
		doAutowired();
		//5.构造HandlerMapping
		initHandlerMapping();
		//6.等待请求，匹配URL，定位方法，反射调用执行
		//调用doGet或者doPost方法
		//提示信息
		System.out.println("my springmvc is success");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("doGet");
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			System.out.println("doPost");
			doDispatch(req,resp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		// TODO Auto-generated method stub
		if(this.handlerMapping.isEmpty()){
			return;
		}
		String url =req.getRequestURI();//   /myspringmvc/demo/user
		String contextPath = req.getContextPath();//   /myspringmvc
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		if(!this.handlerMapping.containsKey(url)){
			resp.getWriter().write("404 Not Found!!");
			return;
		}
		Map<String,String[]> params = req.getParameterMap();
		Method method =this.handlerMapping.get(url);
		//获取方法的参数列表
		Class<?>[] paramerterTypes = method.getParameterTypes();
		//获取请求的参数
		Map<String,String[]> parameterMap  = req.getParameterMap();
		//保留参数值
		Object[] paramValues = new Object[paramerterTypes.length];
		//方法的参数列表
		for (int i = 0; i < paramerterTypes.length; i++) {
			//根据参数名称，做某些处理
			Class parameterType =paramerterTypes[i];
			if(parameterType == HttpServletRequest.class){
				//参数类型已明确，这边强转类型
				paramValues[i] =req;
				continue;
			}else if(parameterType == HttpServletResponse.class){
				paramValues[i] = resp;
				continue;
			}else if(parameterType == String.class){
				for (Entry<String,String[]> param : parameterMap.entrySet()) {
					String value = Arrays.toString(param.getValue())
							.replaceAll("\\[", "").replaceAll("\\]", "")
							.replaceAll("\\&", ",");
					paramValues[i]=value;
					
				}
			}
			
		}
		try {
			String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());//获取源代码中给出的‘底层类’简称
			method.invoke(this.ioc.get(beanName), paramValues);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
 	}

	

	private void initHandlerMapping(){
		   if(ioc.isEmpty()){
		     return;
		   }
		   try {
		     for (Entry<String, Object> entry: ioc.entrySet()) {
		       Class<? extends Object> clazz = entry.getValue().getClass();
		       if(!clazz.isAnnotationPresent(MyController.class)){
		         continue;
		       }
		       
		       //拼url时,是controller头的url拼上方法上的url
		       String baseUrl ="";
		       if(clazz.isAnnotationPresent(MyRequestMapping.class)){
		         MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
		         baseUrl=annotation.value();
		       }
		       Method[] methods = clazz.getMethods();
		       for (Method method : methods) {
		         if(!method.isAnnotationPresent(MyRequestMapping.class)){
		           continue;
		         }
		         MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
		         String url = annotation.value();
		         
		         url =(baseUrl+"/"+url).replaceAll("/+", "/");
		         handlerMapping.put(url,method);
		         System.out.println(url+","+method);
		       }
		       
		     }
		     
		   } catch (Exception e) {
		     e.printStackTrace();
		   }
		   
		 }


	private void doAutowired() {
		// TODO Auto-generated method stub
		if(ioc.isEmpty()){
			return;
		}
		for (Entry<String,Object> entry : ioc.entrySet()) {
			//拿到实例对象中的所有属性
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				if(!field.isAnnotationPresent(MyAutowired.class)){
					continue;
				}
				MyAutowired autowired =field.getAnnotation(MyAutowired.class);
				String beanName = autowired.value().trim();
				if("".equals(beanName)){
					beanName = field.getType().getName();
				}
				field.setAccessible(true);
				try {
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					continue;
				}
			}
		
		
		
		}
		
	}

	private void doInstance() {
		// TODO Auto-generated method stub
		if(classNames.size() == 0){
			return;
		}
		
		try {
			for (String className : classNames) {
				Class<?> clazz = Class.forName(className);
				if(clazz.isAnnotationPresent(MyController.class)){
					String beanName =lowerFirstCase(clazz.getSimpleName());
					ioc.put(beanName, clazz.newInstance());
				}else if(clazz.isAnnotationPresent(MyService.class)){
					MyService service = clazz.getAnnotation(MyService.class);
					String beanName = service.value();
					//如果用户设置了名字，就用用户自己设置的
					if(!"".equals(beanName.trim())){
						ioc.put(beanName,clazz.newInstance());
						continue;
					}
					//如果自己没设，就按接口类型创建一个实例
					Class<?>[] interfaces = clazz.getInterfaces();
					for (Class<?> i : interfaces) {
						ioc.put(i.getName(), clazz.newInstance());
						
					}
				}else if(clazz.isAnnotationPresent(MyRepository.class)){

					MyRepository dao = clazz.getAnnotation(MyRepository.class);
					String beanName = dao.value();
					//如果用户设置了名字，就用用户自己设置的
					if(!"".equals(beanName.trim())){
						ioc.put(beanName,clazz.newInstance());
						continue;
					}
					//如果自己没设，就按接口类型创建一个实例
					Class<?>[] interfaces = clazz.getInterfaces();
					for (Class<?> i : interfaces) {
						ioc.put(i.getName(), clazz.newInstance());
						
					}
				}else{
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String lowerFirstCase(String str) {
		// TODO Auto-generated method stub
		char[] chars =str.toCharArray();
		chars[0]+=32;
		return String.valueOf(chars);
	}

	private void doScanner(String packageName) {
		// TODO Auto-generated method stub
		URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			//如果是文件夹,继续递归
			if(file.isDirectory()){
				doScanner(packageName+"."+file.getName());
			}else{
				classNames.add(packageName+"."+file.getName().replace(".class", "").trim());
			}
		}
	}

	private void doLoadCongig(String location) {
		// TODO Auto-generated method stub
		InputStream fis = null;
		try {
			fis = this.getClass().getClassLoader().getResourceAsStream(location);
			//1.读取配置文件
			p.load(fis);
			System.out.println(p);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			try {
				if(null!=fis){
					fis.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
	}

	
	
}
