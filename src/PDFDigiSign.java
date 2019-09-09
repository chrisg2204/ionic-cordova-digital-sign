package id.kodekreatif.cordova.PDFDigiSign;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
// import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
// import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.StringBuilder;

import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.os.Environment;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Typeface;

import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.cert.X509CertificateHolder;

import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.CMSSignedGenerator;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.Store;

import org.spongycastle.util.encoders.Base64;
import id.co.kodekreatif.pdfdigisign.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.security.KeyStore;
import java.util.Enumeration;
import java.security.Key;
import java.security.UnrecoverableKeyException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.Random;

public class PDFDigiSign extends CordovaPlugin {

	private static final String TAG = "PDFDigiSign";

	CallbackContext callbackContext = null;
  	Context context;
  	private static final int BUFFER_SIZE = 10240;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Log.d(TAG, "Inicializando PDFDigiSign");
	}

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		context = cordova.getActivity(); 
		if (action.equals("sign")) {
			cordova.getThreadPool().execute(new Runnable() {
				public void run() {
					try {
						// Log.d(TAG, "Inicializando PDFDigiSign");
						String ksDecode = getKS(encode("Qwepoi8+"));
						final PluginResult result = new PluginResult(PluginResult.Status.OK, ksDecode);

						// callbackContext.success();
						callbackContext.sendPluginResult(result);
					}
					catch (Exception e) {
						e.printStackTrace();
						callbackContext.error(e.toString()); // Thread-safe.
					}
				}
			});
			// try {
			// 	String ksDecode = getKS(encode("Qwepoi8+"));
			// 	final PluginResult result = new PluginResult(PluginResult.Status.OK, "hola");

			// 	callbackContext.success();
			// 	callbackContext.sendPluginResult(result);
			// } catch (Exception e) {
			// 	e.printStackTrace();
			// 	callbackContext.error(-1);
			// }
		}

		return true;
	}

	public String getKS(String userPassword)
		throws InterruptedException, KeyChainException, UnrecoverableKeyException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

		File filePath = getStorageDir("GCBA-RA");
		Date currentTime = Calendar.getInstance().getTime();
		String filename = "ks.gcba";
		String pdfFileName = "acta.pdf";
		File keystoreFile = new File(filePath, filename);
		File pdfFile = new File(filePath, pdfFileName);
		File signFile = new File(filePath, "blank_sign.bmp");
		// byte[] bytesArray = new byte[(int) signFile.length()]; 
		String result = "";
		try {
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(new FileInputStream(keystoreFile), userPassword.toCharArray());
			Enumeration<String> aliases = keystore.aliases();
			while (aliases.hasMoreElements()) {
				String a = aliases.nextElement();
				// PrivateKey key = keystore.getKey(a, userPassword.toCharArray());
				// Key key = (PrivateKey) keystore.getKey(a, userPassword.toCharArray());
				PrivateKey key = (PrivateKey) keystore.getKey(a, userPassword.toCharArray());
				if (key != null) {
						X509Certificate cert = (X509Certificate) keystore.getCertificate(a);
						Date certExpiryDate = ((X509Certificate) keystore.getCertificate(a)).getNotAfter();

							// signWithAlias("/storage/emulated/0/Documents/GCBA-RA/1.pdf", a, "Christian", "BS AR", "rs", bytesArray, 1, 100, 50, 100, 100);
						byte[] buffer = new byte[BUFFER_SIZE];
						// PrivateKey privKey = KeyChain.getPrivateKey(context, a);
						Certificate[] chain = keystore.getCertificateChain(a);
						File document = new File("/storage/emulated/0/Documents/GCBA-RA/acta.pdf");
						if (!(document != null && document.exists()))
						new RuntimeException("");
						Signature signature = new Signature(chain, key);
						File outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
						outputPath.mkdirs();

						String imgSignPath = signFile.getPath();
						BitmapFactory.Options options = new BitmapFactory.Options();
    					options.inScaled = true;
    					
						Bitmap bitmap = BitmapFactory.decodeFile(imgSignPath, options);
						Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

						File myDir = new File("/storage/emulated/0/Documents/GCBA-RA");
					    myDir.mkdirs();
					    Random generator = new Random();
					    int n = 10000;
					    n = generator.nextInt(n);
					    String fname = "Image-"+ n +".jpg";
					    File file = new File (myDir, fname);
					    if (file.exists ()) file.delete ();

					    FileOutputStream out = new FileOutputStream(file);

			        // NEWLY ADDED CODE STARTS HERE [
			            Canvas canvas = new Canvas(mutableBitmap);

			            Paint paint = new Paint();
			            paint.setFilterBitmap(true);
			            paint.setTypeface(Typeface.SANS_SERIF);
			            paint.setStyle(Paint.Style.FILL);
			            paint.setAntiAlias(true);
			            paint.setSubpixelText(true);
			            paint.setFakeBoldText(true);
			            paint.setElegantTextHeight(true);
			            paint.setColor(Color.BLACK); // Text Color
			            paint.setTextSize(10); // Text Size
			            // paint.setTextScaleX(1); // Text Size
			            // paint.setAntiAlias(true);
			            // paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
			            // some more settings...

			            canvas.drawBitmap(mutableBitmap, 0, 0, paint);
			            canvas.drawText("Digitally signed by " + a, 10, 12, paint);
			            canvas.drawText("DN: " + cert.getSubjectDN().toString(), 10, 25, paint);
			            canvas.drawText(currentTime.toString(), 10, 38, paint);
			        // NEWLY ADDED CODE ENDS HERE ]

			        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			        out.flush();
			        out.close();






						FileInputStream imageStream = new FileInputStream(file);
						byte[] byteArray = new byte[100000];
						imageStream.read(byteArray);
						if (byteArray.length > 0) {
							ByteArrayInputStream image = new ByteArrayInputStream(byteArray);
							signature.setVisual(image, 1, (float)30.0, (float)530.0, (float)1000.0, (float)110.0);
						}

						signature.sign("/storage/emulated/0/Documents/GCBA-RA/acta.pdf", outputPath.getAbsolutePath(), "Christian", "Buenos Aires", "Otra");
						File outputDocument = new File(outputPath.getAbsolutePath() + "/" + document.getName() + ".signed.pdf");
						File resultDocument = new File("/storage/emulated/0/Documents/GCBA-RA/acta.pdf");
						File removeDocument = new File(document.getPath() + ".unsigned.pdf");
						document.renameTo(removeDocument);
						outputDocument.renameTo(resultDocument);

						result = result + "Alias:" + userPassword + " cert:" + cert.getSubjectDN().toString();
				}
			}
		} catch(Exception e) {
			throw e;
		}
		return result;
	}

	public File getStorageDir(String subFolder) {
		File file = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOCUMENTS), subFolder);
		if (!file.mkdirs()) {
			Log.e("ERROR", "Directory not created");
		}
		return file;
	}

	public static String encode(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String salt = "b0e385w8-ad1a-xdfw-a02e-60f67bf0b97f";
		byte[] iv = new byte[16];
		String encrypted = null;
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(salt.getBytes("UTF-8"));
		byte[] bytes = md.digest(key.getBytes("UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100,16).substring(1));
		}

		encrypted = sb.toString();
		return encrypted;
	}
	
	public static void main(String[] args){

		System.out.println("Hello World");
		
	}
}
