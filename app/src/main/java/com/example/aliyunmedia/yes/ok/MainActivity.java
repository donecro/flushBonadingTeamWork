package com.example.aliyunmedia.yes.ok;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import android.support.v7.app.ActionBarActivity;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends AppCompatActivity {
	private TextView tv,detail;
	private Button camerabutton,playbutton,selectvideo;
	private ProgressBar pb;
	private String path,objectname;
	private EditText filename;
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private String typeName;//文件后缀名
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findbyid();
    }
    private void findbyid() {
		// TODO Auto-generated method stub
    	selectvideo= (Button) findViewById(R.id.camerabutton);
    	detail= (TextView) findViewById(R.id.detail);
    	tv = (TextView) findViewById(R.id.text);
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		camerabutton = (Button) findViewById(R.id.camerabutton);
//		playbutton= (Button) findViewById(R.id.playbutton);
		filename=(EditText) findViewById(R.id.filename);


		
		camerabutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	beginupload();
			}
		});
	}

    public void select(View view)
    {
    	//跳到图库
    	  Intent intent = new Intent(Intent.ACTION_PICK);
    	  //选择的格式为视频,图库中就只显示视频（如果图片上传的话可以改为image/*，图库就只显示图片）
    	           intent.setType("image/*;video/*");
    	           // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
    	           startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    public void beginupload(){
    	//通过填写文件名形成objectname,通过这个名字指定上传和下载的文件
    	objectname=filename.getText().toString()+"."+typeName;
    	if(objectname==null||objectname.equals("")){
    		Toast.makeText(MainActivity.this, "文件名不能为空", 2000).show();
    		return;
    	}
    	//填写OSS外网域名
		String endpoint = "http://oss-cn-beijing.aliyuncs.com";
		//填写明文accessKeyId和accessKeySecret
    	OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider("LTAI4nLjBFf9SsUa", "T3Sr5tbOX6hNKAY532S6AnR9a6lkkP");
		OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
		//下面3个参数依次为bucket名，Object名，上传文件路径
    	PutObjectRequest put = new PutObjectRequest("miemiemie", objectname, path);
    	if(path==null||path.equals("")){
    		detail.setText("请选择上传文件");
    		return;
    	}
		tv.setText("正在上传中....");
		pb.setVisibility(View.VISIBLE);
    	// 异步上传，可以设置进度回调
    	put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
    		@Override
    	    public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
				// 在这里可以实现进度条展现功能
    	    		Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
				}
    	});
    	@SuppressWarnings("rawtypes")
		OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
    	    @Override
    	    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
    	        Log.d("PutObject", "UploadSuccess");
    	        //去UI线程更新UI
    	    	runOnUiThread(new Runnable() {
   				@Override
    				public void run() {
    					// TODO Auto-generated method stub
					  tv.setText("UploadSuccess");
					  pb.setVisibility(ProgressBar.INVISIBLE);
					initData(objectname);
					}
    			});
    	    }
    	    @Override
    	    public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
    	        // 请求异常
    	    	runOnUiThread(new Runnable() {
       				@Override
        				public void run() {
        					// TODO Auto-generated method stub
       					pb.setVisibility(ProgressBar.INVISIBLE);
       	    	    	tv.setText("Uploadfile,localerror");
       				}
        			});
    	        if (clientExcepion != null) {
    	            // 本地异常如网络异常等
    	            clientExcepion.printStackTrace();
    	        }
    	        if (serviceException != null) {
    	            // 服务异常
    	        	tv.setText("Uploadfile,servererror");
    	            Log.e("ErrorCode", serviceException.getErrorCode());
    	            Log.e("RequestId", serviceException.getRequestId());
    	            Log.e("HostId", serviceException.getHostId());
    	            Log.e("RawMessage", serviceException.getRawMessage());
    	        }
    	    }
    	});
}
         @Override
		 //返回关闭图库后得到的数据
		 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			 if (requestCode == PHOTO_REQUEST_GALLERY) {
				 // 从相册返回的数据
				 if (data != null) {
					 // 得到视频的全路径
					 Uri uri = data.getData();
					 getRealFilePath(MainActivity.this,uri);
				 }
			 }
			 super.onActivityResult(requestCode, resultCode, data);
		 }
        /* 通过Uri获取路径以及文件名，比如得到的路径 /xx/xx/xx/xx/xxx.jpg，
                                 通过索引最后一个/就可以在String中截取了*/
         public  void getRealFilePath( final Context context, final Uri uri ) {
             if ( null == uri ) return ;
             final String scheme = uri.getScheme(); //返回当前链接使用的协议
             String data = null;
             if ( scheme == null )
                 data = uri.getPath();
             else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
                 data = uri.getPath();
             } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
                 Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
                 if ( null != cursor ) {
                     if ( cursor.moveToFirst() ) {
                         int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                         if ( index > -1 ) {
                             data = cursor.getString( index );
                         }
                     }
                     cursor.close();
                 }
             }
             path=data;
             String b = path.substring(path.lastIndexOf("/") + 1, path.length());
			 int dot = b.lastIndexOf('.');
			 if ((dot >-1) && (dot < (b.length() - 1))) {
				 typeName=b.substring(dot + 1);
			 }

			 detail.setText(b);

         }

	private String logPath;
	private void initData(String name) {
		String filePath = "/sdcard/Test/";
		String fileName = "log.txt";
		logPath=filePath+fileName;
		writeTxtToFile(name, filePath, fileName);
		PutObjectRequest put2 = new PutObjectRequest("miemiemie", fileName, logPath);
		put2.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
			@Override
			public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {

			}
		});
		//填写OSS外网域名
		String endpoint = "http://oss-cn-beijing.aliyuncs.com";
		//填写明文accessKeyId和accessKeySecret
		OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider("LTAI4nLjBFf9SsUa", "T3Sr5tbOX6hNKAY532S6AnR9a6lkkP");
		OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider);
		OSSAsyncTask task = oss.asyncPutObject(put2, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
			@Override
			public void onSuccess(PutObjectRequest request, PutObjectResult result) {
				Log.d("PutObject", "UploadSuccess");
				//去UI线程更新UI
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						tv.setText("UploadSuccess");
						pb.setVisibility(ProgressBar.INVISIBLE);
					}
				});
			}
			@Override
			public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
				// 请求异常
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pb.setVisibility(ProgressBar.INVISIBLE);
						tv.setText("Uploadfile,localerror");
					}
				});
				if (clientExcepion != null) {
					// 本地异常如网络异常等
					clientExcepion.printStackTrace();
				}
				if (serviceException != null) {
					// 服务异常
					tv.setText("Uploadfile,servererror");
					Log.e("ErrorCode", serviceException.getErrorCode());
					Log.e("RequestId", serviceException.getRequestId());
					Log.e("HostId", serviceException.getHostId());
					Log.e("RawMessage", serviceException.getRawMessage());
				}
			}
		});
	}

	// 将字符串写入到文本文件中
	public void writeTxtToFile(String strcontent, String filePath, String fileName) {
		//生成文件夹之后，再生成文件，不然会出错
		makeFilePath(filePath, fileName);
		String strFilePath = filePath+fileName;
		// 每次写入时，都换行写
		String strContent = strcontent + "\r\n";
		try {
			File file = new File(strFilePath);
			if (!file.exists()) {
				Log.d("TestFile", "Create the file:" + strFilePath);
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rwd");
			raf.seek(file.length());
			raf.write(strContent.getBytes());
			raf.close();
		} catch (Exception e) {
			Log.e("TestFile", "Error on write File:" + e);
		}
	}

	// 生成文件
	public File makeFilePath(String filePath, String fileName) {
		File file = null;
		makeRootDirectory(filePath);
		try {
			file = new File(filePath + fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	// 生成文件夹
	public static void makeRootDirectory(String filePath) {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdir();
			}
		} catch (Exception e) {
			Log.i("error:", e+"");
		}
	}
}
