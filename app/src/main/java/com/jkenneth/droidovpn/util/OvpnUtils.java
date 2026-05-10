package com.jkenneth.droidovpn.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;

import com.jkenneth.droidovpn.R;
import com.jkenneth.droidovpn.model.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Jhon Kenneth Carino on 10/18/15.
 */
public class OvpnUtils {

    private static final String FILE_EXTENSION = ".ovpn";
    private static final String OPENVPN_PKG_NAME = "net.openvpn.openvpn";
    private static final String OPENVPN_MIME_TYPE = "application/x-openvpn-profile";

    public static void importToOpenVpn(@NonNull final Activity activity, @NonNull Server server) {
        File file = getFile(activity, server);
        if (!file.exists()) {
            saveConfigData(activity, server);
        }

        Uri uri = FileProvider.getUriForFile(activity,
                activity.getApplicationContext().getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, OPENVPN_MIME_TYPE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        List<ResolveInfo> resolvedIntentActivities = activity.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            activity.grantUriPermission(resolvedIntentInfo.activityInfo.packageName, uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(R.string.title_import_dialog)
                    .setMessage(R.string.message_import_dialog)
                    .setCancelable(false)
                    .setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PlayStoreUtils.openApp(activity, OPENVPN_PKG_NAME);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    public static void shareOvpnFile(@NonNull Activity activity, @NonNull Server server) {
        File file = getFile(activity, server);
        if (!file.exists()) {
            saveConfigData(activity, server);
        }

        Uri uri = FileProvider.getUriForFile(activity,
                activity.getApplicationContext().getPackageName() + ".fileprovider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(OPENVPN_MIME_TYPE);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        activity.startActivity(Intent.createChooser(shareIntent, "Share Profile using"));
    }

    public static String humanReadableCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.2f %s" + (si ? "bps" : "B"),
                bytes / Math.pow(unit, exp), pre);
    }

    private static void saveConfigData(@NonNull Context context, @NonNull Server server) {
        File file = getFile(context, server);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(server.ovpnConfigData.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) outputStream.close();
            } catch (Exception ignored) {}
        }
    }

    private static File getFile(@NonNull Context context, @NonNull Server server) {
        File filePath = context.getCacheDir();
        return new File(filePath, server.countryShort + "_" + server.hostName + "_" +
                server.protocol.toUpperCase() + FILE_EXTENSION);
    }

    public static int getDrawableResource(@NonNull Context context, @NonNull String resource) {
        return context.getResources()
                .getIdentifier(resource, "drawable", context.getPackageName());
    }
}
