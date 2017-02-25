package com.example.erika.cookbook;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class Recept extends FragmentActivity {
    private TextView TVnazev_receptu;
    private String stringNazevReceptu = "", nazev_receptu;
    private ReceptyTable DBrecepty;
    private SurovinaReceptTable DBsurovina_recept;
    private int ID_receptu;
    private Cursor recept, image;
    private ImageView foto;
    final Handler handler = new Handler();
    public ProgressDialog progressDialog;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
    private static final int IMAGE_FROM_FILE_ACTIVITY_REQUEST_CODE = 1;
    private Uri picUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recept);

        TVnazev_receptu = (TextView) findViewById(R.id.nazev_receptuTV);
        DBrecepty = ReceptyTable.getInstance(this);
        DBsurovina_recept = SurovinaReceptTable.getInstance(this);
        foto = (ImageView) findViewById(R.id.imageViewFoto);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

    }

    @Override
    protected void onStart() {
        Bundle extras = getIntent().getExtras();
        final LongOperationsThreadRecept MyThreadRecept = new LongOperationsThreadRecept();
        if(extras != null) {
            ID_receptu = extras.getInt("id_receptu");
            nazev_receptu = extras.getString("nazev_receptu");
            if (nazev_receptu != null){ //pri otevreni receptu z vyhledanych surovin podle nazvu receptu
                MyThreadRecept.execute("acc_to_name");
            }
            if(ID_receptu > 0){ //pri otevreni receptu ze seznamu receptu podle ID receptu
                MyThreadRecept.execute("get_recept");
            }
            LongOperationsThreadGetImage MyThreadGetImage = new LongOperationsThreadGetImage();
            MyThreadGetImage.execute();
        }


        super.onStart();
    }

    private void setReceptName(Cursor recept){
        stringNazevReceptu = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_NAZEV_RECEPTU));
        TVnazev_receptu.setText(stringNazevReceptu);
        //recept.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recept, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rate) {
            Intent hodnoceni = new Intent(getApplicationContext(), HodnoceniReceptu.class);
            Bundle recept = new Bundle();
            recept.putString("nazev_receptu", nazev_receptu);
            hodnoceni.putExtras(recept);
            startActivity(hodnoceni);
            return true;
        }
        else if (id == R.id.action_photo){
            selectImage();
        }
        else if (id == R.id.action_update){
            Intent intent = new Intent(getApplicationContext(), NovyRecept.class);
            Bundle dataBundle = new Bundle();
            dataBundle.putString("nazev_receptu", nazev_receptu);
            intent.putExtras(dataBundle);
            startActivity(intent);
        }
        else if(id == R.id.action_deleteRecept){
            //smazani receptu - alert dialog na potvrzeni + pokud ano tak smazat polozku z DB
            AlertDialog.Builder builder = new AlertDialog.Builder(Recept.this);
            builder.setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title);
            builder.setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //doNothing
                }
            });
            builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // smazat polozku z DB
                    Bundle kategorieID = new Bundle();
                    Cursor receptCursor = DBrecepty.getReceptAccordingToName(nazev_receptu);
                    receptCursor.moveToFirst();
                    int IDkategorie = Integer.parseInt(receptCursor.getString(receptCursor.getColumnIndex(DBrecepty.COLUMN_ID_KATEGORIE)));
                    kategorieID.putInt("id", IDkategorie);
                    String ID_receptu = receptCursor.getString(receptCursor.getColumnIndex(DBrecepty.COLUMN_ID));
                    DBrecepty.deleteRecept(ID_receptu);
                    DBsurovina_recept.deleteSurovinaRecept(nazev_receptu);

                    Toast.makeText(getApplicationContext(), R.string.item_deleted + " (" + nazev_receptu + ").", Toast.LENGTH_SHORT).show();
                    Intent mainActivity = new Intent(Recept.this, MainActivity.class);
                    startActivity(mainActivity);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if (id == R.id.action_shoppingList){
            Intent nakupniSeznam = new Intent(getApplicationContext(), NakupniSeznam.class);
            startActivity(nakupniSeznam);
        }
        else if (id == R.id.action_share){
            shareIntentSpecificApps();
        }
        return super.onOptionsItemSelected(item);
    }



    private void selectImage() {
        final CharSequence[] items = { "Vyfoť nový obrázek", "Vyber obrázek z galerie",
                "Zrušit" };
        AlertDialog.Builder builder = new AlertDialog.Builder(Recept.this);
        builder.setTitle("Přidat obrázek ...");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Vyfoť nový obrázek")) {
                    Intent ziskatFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ziskatFoto.putExtra(MediaStore.EXTRA_OUTPUT, picUri); // set the image file name
                    startActivityForResult(ziskatFoto, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                } else if (items[item].equals("Vyber obrázek z galerie")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);//
                    startActivityForResult(Intent.createChooser(intent, "Select File"), IMAGE_FROM_FILE_ACTIVITY_REQUEST_CODE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                onCaptureImageResult(data);
                Toast.makeText(this, "Obrázek byl uložen\n" + picUri + "\n do databáze.", Toast.LENGTH_LONG).show();
                DBrecepty.insertImagePath(nazev_receptu, picUri.getPath());
            } else if (requestCode == IMAGE_FROM_FILE_ACTIVITY_REQUEST_CODE) {
                onSelectFromGalleryResult(data);
                DBrecepty.insertImagePath(nazev_receptu, picUri.getPath());
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            Toast toast = Toast.makeText(this, "Focení receptu zrušeno.", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "Něco se pokazilo ... :(", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void onCaptureImageResult(Intent data) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Kucharka");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Kucharka", "failed to create directory");
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        File destination = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        picUri = Uri.fromFile(destination);

    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                Uri selectedImageURI = data.getData();
                File destination = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera" + File.separator + getRealPathFromURI(selectedImageURI));
                picUri = Uri.fromFile(destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String picturePath = null;
        Uri _uri = contentURI;
        if (_uri != null && "content".equals(_uri.getScheme())) {
            String[] filePath = { MediaStore.Images.Media.DISPLAY_NAME };
            Cursor cursor = this.getContentResolver().query(_uri, filePath, null, null, null);
            cursor.moveToFirst();
            picturePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            cursor.close();
        } else {
            picturePath = _uri.getPath();
        }
        return picturePath;
    }

    public void shareIntentSpecificApps() {
        List<Intent> intentShareList = new ArrayList<Intent>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);

        for (ResolveInfo resInfo : resolveInfoList) {
            String packageName = resInfo.activityInfo.packageName;
            String name = resInfo.activityInfo.name;

            if (packageName.contains("com.facebook") ||
                    packageName.contains("com.twitter.android") ||
                    packageName.contains("com.google.android.apps.plus") ||
                    packageName.contains("mms") ||
                    packageName.contains("com.google.android.talk") ||
                    packageName.contains("com.google.android.gm")) {

                if (name.contains("com.twitter.android.DMActivity")) {
                    continue;
                }

                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Recept - " + nazev_receptu);

                intent.putExtra(Intent.EXTRA_TEXT, nazev_receptu + "\n\n" + ingredientsOfRecipe(recept) +
                        "\n\n" + contentOfRecipe(recept) + "\n\n" + evaluationOfRecipe(recept));
                intentShareList.add(intent);
            }
        }

        if (intentShareList.isEmpty()) {
            Toast.makeText(Recept.this, "Nenalezeny žádné aplikace, pomocí kterých lze recept sdílet", Toast.LENGTH_SHORT).show();
        } else {
            Intent chooserIntent = Intent.createChooser(intentShareList.remove(0), "Sdílej recept pomocí ...");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentShareList.toArray(new Parcelable[]{}));
            startActivity(chooserIntent);
        }
    }

    public String ingredientsOfRecipe(Cursor recept){
        recept.moveToFirst();
        String pocetPorci = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POCET_PORCI));
        String ingredientsOfRecipe = "Recept je na " + pocetPorci + " porce." + "\n\n" + "Ingredience: ";

        final ArrayList<SurovinaReceptO> ALsurovinaRecept = DBsurovina_recept.getSurovinaRecept(nazev_receptu);
        for (SurovinaReceptO surovinaReceptObj : ALsurovinaRecept){
            ingredientsOfRecipe = ingredientsOfRecipe + "\n" +
                    surovinaReceptObj.mnozstvi + surovinaReceptObj.typ_mnozstvi + " " + surovinaReceptObj.surovina;
        }


        return ingredientsOfRecipe;
    }

    public String contentOfRecipe(Cursor recept) {
        recept.moveToFirst();
        String postup = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_POSTUP));
        String dobaPeceni = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PECENI));
        String dobaPripravy = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_DOBA_PRIPRAVY));
        String prilohy = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_PRILOHY));
        String stupne = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_STUPNE));

        if (prilohy == null || prilohy == ""){
            prilohy = "-žádné uložené-";
        }

        String contentOfRecipe = "Příprava: " + dobaPripravy + "min \n" + "Tepelná úprava: " +
                dobaPeceni + "min (na " + stupne + "°C)\n\n" + "Postup: \n " + postup +"\n" + "\nPřílohy: " + prilohy;
        return contentOfRecipe;
    }

    public String evaluationOfRecipe(Cursor recept){
        recept.moveToFirst();
        String evaluationOfRecipe = "";
        int hodnoceni = recept.getInt(recept.getColumnIndex(DBrecepty.COLUMN_HODNOCENI));
        String komentar = recept.getString(recept.getColumnIndex(DBrecepty.COLUMN_KOMENTAR));

        if (komentar == null || komentar.equals("") || komentar.equals(" ")){
            komentar = "-žádný uložený-";
        }

        evaluationOfRecipe = "Mé hodnocení receptu: \n Počet hvězdiček: " + hodnoceni + "\n Komentář: " + komentar;
        return evaluationOfRecipe;
    }

    public void setImage(Cursor image){
        if (image != null) {
            final String imagePathString = image.getString(image.getColumnIndex(DBrecepty.COLUMN_FOTO));
            if (imagePathString != null) {
                //final Bitmap imageBitmap = BitmapFactory.decodeFile(imagePathString);
                //final Bitmap scaled = scaleDownBitmap(imageBitmap, 160, getApplicationContext());
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                final Bitmap imageBitmap = BitmapFactory.decodeFile(imagePathString, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                int scaleFactor = 1;
                if ((260 > 0) || (160 > 0)) {
                    scaleFactor = Math.min(photoW/260, photoH/160);
                }

                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                final Bitmap smallerImageBitmap = BitmapFactory.decodeFile(imagePathString, bmOptions);
                foto.setImageBitmap(smallerImageBitmap);
                foto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Recept.this, FullScreenImage.class);
                        intent.putExtra("image", imagePathString);
                        startActivity(intent);
                    }
                });
            } else {
                foto.setImageResource(R.drawable.noimagefound);
            }
        }
        image.close();
    }

    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo=Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LongOperationsThreadRecept longOperationsThreadRecept = new LongOperationsThreadRecept();
        longOperationsThreadRecept.execute("acc_to_name");

        LongOperationsThreadGetImage MyThreadGetImage = new LongOperationsThreadGetImage();
        MyThreadGetImage.execute();


    }

    private class LongOperationsThreadRecept extends AsyncTask<String, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            handler.postDelayed(pdRunnable, 500);
        }
        @Override
        protected void onPostExecute(Cursor recept) {
            super.onPostExecute(recept);

            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            recept.moveToFirst();
            setReceptName(recept);

        }

        @Override
        protected Cursor doInBackground(String... strings) {
            String method = strings[0];
            if (method.equals("acc_to_name")){
                recept = DBrecepty.getReceptAccordingToName(nazev_receptu);
            }
            else if (method.equals("get_recept")){
                recept = DBrecepty.getRecept(ID_receptu);
            }

            return recept;
        }
        final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(Recept.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };
    }
    private class LongOperationsThreadGetImage extends AsyncTask<String, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pdRunnable.run();
            handler.postDelayed(pdRunnable, 100);

        }
        @Override
        protected void onPostExecute(Cursor image) {
            super.onPostExecute(image);
            handler.removeCallbacks(pdRunnable);
            if (progressDialog!=null) {
                progressDialog.dismiss();
            }

            image.moveToFirst();
            setImage(image);
        }

        @Override
        protected Cursor doInBackground(String... strings) {
            //
            DBrecepty = ReceptyTable.getInstance(Recept.this);
            recept = DBrecepty.getReceptAccordingToName(nazev_receptu);
            recept.moveToFirst();
            ID_receptu = recept.getInt(recept.getColumnIndex(DBrecepty.COLUMN_ID));
            image = DBrecepty.getImagePath(ID_receptu);
            return image;
        }
        final Runnable pdRunnable = new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(Recept.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        };
    }
}
