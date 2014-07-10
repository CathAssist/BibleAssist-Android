package org.cathassist.bible.lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.umeng.socialize.bean.CustomPlatform;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeConfig;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;

import org.cathassist.bible.R;

public class Share {
    public static void shareToSns(Activity context, String content) {
        final UMSocialService mController = UMServiceFactory.getUMSocialService("share",
                RequestType.SOCIAL);
        SocializeConfig config = mController.getConfig();
        mController.setShareContent(content);
        config.setPlatformOrder(SHARE_MEDIA.SMS, SHARE_MEDIA.EMAIL, SHARE_MEDIA.SINA,
                                SHARE_MEDIA.TENCENT, SHARE_MEDIA.QZONE);
        config.removePlatform(SHARE_MEDIA.RENREN, SHARE_MEDIA.DOUBAN);
        config.addCustomPlatform(getOtherTextShare(context, content));
        mController.openShare(context, false);
    }

    private static CustomPlatform getOtherTextShare(final Context context, final String content) {
        CustomPlatform otherShare = new CustomPlatform("更多", R.drawable.umeng_socialize_more_on);
        otherShare.mKeyword = "custom_other";
        otherShare.mClickListener = new SocializeListeners.OnCustomPlatformClickListener() {
            @Override
            public void onClick(CustomPlatform customPlatform, SocializeEntity socializeEntity, SocializeListeners.SnsPostListener snsPostListener) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, "分享到..."));
            }
        };
        return otherShare;
    }

    public static void shareByAndroid(Context context, String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, "分享到..."));
    }
}
