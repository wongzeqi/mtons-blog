/*
+--------------------------------------------------------------------------
|   Mblog [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package mblog.web.controller.desk.account;

import java.io.File;

import mblog.base.utils.ImageHandleUtils;
import mblog.base.utils.ImageUtils;
import mblog.core.persist.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import mblog.base.context.AppContext;
import mblog.base.utils.FilePathUtils;
import mblog.core.data.AccountProfile;
import mblog.core.persist.service.UserService;
import mblog.web.controller.BaseController;
import mblog.web.controller.desk.Views;
import mtons.modules.pojos.Data;
import mtons.modules.utils.GMagickUtils;

/**
 * @author langhsu
 *
 */
@Controller
@RequestMapping("/account")
public class AvatarController extends BaseController {
	@Autowired
	private AppContext appContext;
	@Autowired
	private UserService userService;

	@RequestMapping(value = "/avatar", method = RequestMethod.GET)
	public String view() {
		return getView(Views.ACCOUNT_AVATAR);
	}
	
	@RequestMapping(value = "/avatar", method = RequestMethod.POST)
	public String post(String path, Float x, Float y, Float width, Float height, ModelMap model) {
		AccountProfile profile = getSubject().getProfile();

		System.out.println("开始保存头像....");
		if (StringUtils.isEmpty(path)) {
			model.put("data", Data.failure("请选择图片"));
			return getView(Views.ACCOUNT_AVATAR);
		}
		
		if (width != null && height != null) {
			String root = fileRepoFactory.select().getRoot();
			File temp = new File(root + path);
			File scale = null;
			
			// 目标目录
			String ava100 = appContext.getAvaDir() + getAvaPath(profile.getId(), 100);
			String dest = root + ava100;
			try {
				// 判断父目录是否存在
				File f = new File(dest);
		        if(!f.getParentFile().exists()){
		            f.getParentFile().mkdirs();
		        }
		        // 在目标目录下生成截图
		        String scalePath = f.getParent() + "/" + profile.getId() + ".jpg";
		        ImageHandleUtils.truncateImage(temp.getAbsolutePath(), scalePath, x.intValue(), y.intValue(), width.intValue());
		        
				// 对结果图片进行压缩
				ImageHandleUtils.scaleImage(scalePath, dest, 100);

				AccountProfile user = userService.updateAvatar(profile.getId(), ava100);
				putProfile(user);
				
				scale = new File(scalePath);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				temp.delete();
				if (scale != null) {
					scale.delete();
				}
			}
		}
		System.out.println("开始保存头像成功....");
		return "redirect:/account/profile";
	}
	
	public String getAvaPath(long uid, int size) {
		String base = FilePathUtils.getAvatar(uid);
		return String.format("/%s_%d.jpg", base, size);
	}
}
