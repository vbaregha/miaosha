# 秒杀项目







## 集成MyBatis





## 集成Redis




## 登录功能
### 两次MD5

有成熟的框架可以替代两次MD5校验模式

```java
//	MD5产生函数
	public static String md5(String src) {
		return DigestUtils.md5Hex(src);
	}
//	固定颜值
	private static final String salt = "1a2b3c4d";

//	将输入的密码进行第一次MD5
	public static String inputPassToFormPass(String inputPass) {
		String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
		System.out.println(str);
		return md5(str);
	}
//	进行第二次MD5
	public static String formPassToDBPass(String formPass, String salt) {
		String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}
	
	public static String inputPassToDbPass(String inputPass, String saltDB) {
		String formPass = inputPassToFormPass(inputPass);
		String dbPass = formPassToDBPass(formPass, saltDB);
		return dbPass;
	}
	
```

### 验证手机号

#### c3p0参数校验

```java
<!--参数校验-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    

```

```java
    @NotNull
	@IsMobile//自定义注解
	private String mobile;
	
	@NotNull
	@Length(min=32)
	private String password;
```

#### 自定义注解校验

@IsMobile//自定义注解写法

```java
package com.imooc.miaosha.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class })
public @interface  IsMobile {
	
	boolean required() default true;
	
	String message() default "手机号码格式错误";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
```

需要调用处理器(validatedBy = {IsMobileValidator.class })

```java
package com.imooc.miaosha.validator;
import  javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.imooc.miaosha.util.ValidatorUtil;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

	private boolean required = false;
	
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(required) {
			return ValidatorUtil.isMobile(value);
		}else {
			if(StringUtils.isEmpty(value)) {
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}

}
```

#### 全局异常处理

[全局异常处理知识](https://www.cnblogs.com/lvbinbin2yujie/p/10574812.html)

自定义全局异常：

```java
package com.imooc.miaosha.exception;

import com.imooc.miaosha.result.CodeMsg;

public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm) {
		super(cm.toString());
		this.cm = cm;
	}

	public CodeMsg getCm() {
		return cm;
	}

}
```

拦截处理：

```java
package com.imooc.miaosha.exception;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	@ExceptionHandler(value=Exception.class)
	public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
		e.printStackTrace();
		if(e instanceof GlobalException) {
			GlobalException ex = (GlobalException)e;
			return Result.error(ex.getCm());
		}else if(e instanceof BindException) {
			BindException ex = (BindException)e;
			List<ObjectError> errors = ex.getAllErrors();
			ObjectError error = errors.get(0);
			String msg = error.getDefaultMessage();
			return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
		}else {
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
}

```



### get转发，post处理

处理地址栏请求得到页面，处理post请求确认登陆

```java
@RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }
    
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
    	//日志输出
        log.info(loginVo.toString());
    	//登录验证
        miaoshaUserService.login(response, loginVo);

    	return Result.success(true);
    }
```
## 优化方向

### 1. 压力测试

Jmeter进行压测。

#### 测试方向

- 测试服务同时可支撑的并发请求商品列表页面的访问量
- 测试返回Json数据可以支撑的并发访问量
- 秒杀业务的可支撑的并发访问量

#### 测试结果

- 每次刷新页面，系统都返回整个页面，是访问性能瓶颈
- 返回Json数据明显可以支撑更多的并发访问请求
- 没有进行数据库唯一索引的情况下，出现超卖的严重错误。
- 每个请求都访问了数据库，数据库是秒杀业务性能瓶颈

#### 优化方向

性能方向：

- 可以将页面进行缓存，CDN，
- 浏览器AJax进行异步Json请求，更新页面不必重新加载整个网页。
- Redis预减库存。Redis卖完后进行标记。
- 消息队列异步处理秒杀请求。
- 当库存减扣完毕，直接抛弃秒杀请求。

安全方向：

- 数据库唯一索引，防止超卖。也可以将用户Id设为主键。
- 秒杀接口隐藏
- 禁止重复秒杀

### RabbitMQ

消息队列

### Redis缓存



# 错误处理

#### 1.报错

```java
Template might not exist or might not be accessible
```

[处理方法](https://blog.csdn.net/liming_0820/article/details/80878168)

```
本人重新修改了Resouce目录，重新加载了一次，简直莫名其妙
```

#### 2.静态资源映射

```java
# 配置静态资源访问前缀
#spring.mvc.static-path-pattern=/**
# 配置静态资源路径，默认配置失效
#spring.resources.static-locations[0]=classpath:/

https://blog.csdn.net/u010358168/article/details/81205116
# 配置静态资源访问前缀
spring.mvc.static-path-pattern=/mystatic/**
# 配置静态资源路径，默认配置失效
spring.resources.static-locations[0]=classpath:/mystatic
spring.resources.static-locations[1]=classpath:/public
```

删除配置后反而可以获取图片资源。。。



#### 3. 解决超卖

```mysql
update miaosha_goods 
set stock_count = stock_count - 1 
where goods_id = #{goodsId}
```

该语句并没有利用MySQL的排他锁例如：good_id > 0;

#### 4.解决重复秒杀

前端：

前端禁止秒杀请求重复提交。

后端：

利用数据库建立唯一索引



