package com.anaya.vci.qr_activos;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mzXingScannerView;
    Button btn_scan, btn_ok;
    Dialog customDialog;
    TextView txt_categoria,txt_sucursal, txt_modelo, txt_funcionario, txt_nro_serie,txt_marca,txt_codigo_siaf, txt_descripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init(){
        validarPermisos();
        customDialog = new Dialog(this);
        btn_scan = (Button)findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mzXingScannerView = new ZXingScannerView(MainActivity.this);
                setContentView(mzXingScannerView);
                mzXingScannerView.setResultHandler(MainActivity.this);
                mzXingScannerView.startCamera();
            }
        });
    }

    /**
     *
     * @param result del codigo de barras
     */
    @Override
    public void handleResult(Result result) {
        Log.v("HandleResult",result.getText());
        setContentView(R.layout.activity_main);
        mzXingScannerView.stopCamera();
        init();
        String respuesta = result.getText();
        String[] data = respuesta.split("&");
        String compare = data[0];
        if ( compare.equals("anaya")){
            showInfoActivo(data);
        }else{
            Toast.makeText(getApplicationContext(),"ESTE QR NO COINCIDE CON NUESTROS REGISTROS",Toast.LENGTH_SHORT).show();
        }

    }

    private void showInfoActivo(String[] data) {
        customDialog.setContentView(R.layout.info_activo);
        btn_ok = (Button)customDialog.findViewById(R.id.btn_ok);
        txt_categoria = (TextView)customDialog.findViewById(R.id.txt_categoria);
        txt_modelo = (TextView)customDialog.findViewById(R.id.txt_modelo);
        txt_sucursal = (TextView)customDialog.findViewById(R.id.txt_sucursal);
        txt_funcionario = (TextView)customDialog.findViewById(R.id.txt_funcionario);
        txt_nro_serie = (TextView)customDialog.findViewById(R.id.txt_nro_serie);
        txt_marca = (TextView)customDialog.findViewById(R.id.txt_marca);
        txt_codigo_siaf = (TextView)customDialog.findViewById(R.id.txt_codigo_siaf);
        txt_descripcion = (TextView)customDialog.findViewById(R.id.txt_descripcion);

        txt_categoria.setText((data[1].isEmpty())?"--":data[1]);
        txt_sucursal.setText((data[2].isEmpty())?"--":data[2]);
        txt_funcionario.setText((data[3].isEmpty())?"--":data[3]);
        txt_nro_serie.setText((data[4].isEmpty())?"--":data[4]);
        txt_marca.setText((data[5].isEmpty())?"--":data[5]);
        txt_codigo_siaf.setText((data[6].isEmpty())?"--":data[6]);
        txt_modelo.setText((data[7].isEmpty())?"--":data[7]);
        txt_descripcion.setText((data[8].isEmpty())?"--":data[8]);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.show();
    }

    private boolean validarPermisos(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }
        if ((checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED)){
            return true;
        }
        
        if ((shouldShowRequestPermissionRationale(CAMERA))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{CAMERA},100);
        }
        return false;
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Permisos desactivados");
        dialog.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");
        dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{CAMERA},100);
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10){
            if (grantResults.length == 1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

            }else {
                solicitarPermisosManual();
            }
        }
    }

    private void solicitarPermisosManual() {
        final CharSequence[] opciones = {"si","no"};
        AlertDialog.Builder alertOpciones = new AlertDialog.Builder(MainActivity.this);
        alertOpciones.setTitle("Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (opciones[which].equals("si")){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Los permisos no fueron aceptados",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        alertOpciones.show();
    }
}
