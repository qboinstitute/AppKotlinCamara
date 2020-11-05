package com.qbo.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private val CAMARA_REQUEST = 1888
    var mRutaFotoActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btntomarfoto.setOnClickListener {
            if(permisoEscrituraAlmacenamiento()){
                //Toast.makeText(applicationContext, "Puede tomar foto", Toast.LENGTH_LONG).show()
                intencionTomarFoto()
            }else{
                solicitarPermiso()
            }
        }
    }

    private fun permisoEscrituraAlmacenamiento() : Boolean{
        val permiso = ContextCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var resultado = false
        if(permiso == PackageManager.PERMISSION_GRANTED) resultado = true
        return resultado
    }

    private fun solicitarPermiso(){
        ActivityCompat.requestPermissions(this,
        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
        0)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if(requestCode == 0){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Permiso Aceptado", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(applicationContext, "Permiso Denegado", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun crearArchivoImagen(): File?{
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imagenFileName = "JPG_$timeStamp"+"_"
        val storageDir : File = this?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val imagen: File = File.createTempFile(imagenFileName,".jpg", storageDir)
        mRutaFotoActual = imagen.absolutePath
        return imagen
    }
    @Throws(IOException::class)
    private fun intencionTomarFoto(){
        val tomarFotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(tomarFotoIntent.resolveActivity(this?.packageManager!!) != null){
            val archivoFoto = crearArchivoImagen()
            if(archivoFoto != null){
                val urlfoto : Uri? = FileProvider.getUriForFile(
                        applicationContext,
                        "com.qbo.myapplication.provider",
                        archivoFoto
                )
                tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, urlfoto)
                startActivityForResult(tomarFotoIntent, CAMARA_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CAMARA_REQUEST){
            grabarFoto()
            mostrarFoto()
        }

    }

    private fun grabarFoto() {
        val mediaScanIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val nuevoarchivo = File(mRutaFotoActual)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val contenidoUri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.qbo.myapplication.provider",
                    nuevoarchivo)
            mediaScanIntent.data = contenidoUri
        }else{
            val contenidoUri = Uri.fromFile(nuevoarchivo)
            mediaScanIntent.data = contenidoUri
        }
        this?.sendBroadcast(mediaScanIntent)
    }

    private fun mostrarFoto() {
        val einterfaz = ExifInterface(mRutaFotoActual)
        val orientacion: Int = einterfaz.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
        )
        if(orientacion == ExifInterface.ORIENTATION_ROTATE_90){
            ivfoto.rotation = 90.0F
        }else{
            ivfoto.rotation = 0.0F
        }
        val ancho : Int = ivfoto.width
        val alto: Int = ivfoto.height
        val bmOpcion = BitmapFactory.Options()
        bmOpcion.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mRutaFotoActual, bmOpcion)
        val fotoancho = bmOpcion.outWidth
        val fotoalto = bmOpcion.outHeight
        val scaleFactor = min(ancho/ fotoancho, alto / fotoalto)
        bmOpcion.inSampleSize = scaleFactor
        bmOpcion.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(mRutaFotoActual, bmOpcion)
        ivfoto.setImageBitmap(bitmap)
    }

}