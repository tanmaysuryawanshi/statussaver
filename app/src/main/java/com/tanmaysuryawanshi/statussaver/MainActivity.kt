package com.tanmaysuryawanshi.statussaver

import android.app.Application
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.tanmaysuryawanshi.statussaver.fragments.PhotoFragment
import com.tanmaysuryawanshi.statussaver.fragments.VideoFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

private lateinit var rvStatusList:RecyclerView
    private lateinit var statusList:ArrayList<ModelClass>
    private lateinit var photoList:ArrayList<ModelClass>
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var tabLayout: TabLayout

    private lateinit var fragmentList: ArrayList<Fragment>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        analytics = Firebase.analytics
        supportActionBar!!.hide()
        rvStatusList=findViewById(R.id.rvStatus)
        statusList= ArrayList()

        val result=readDataFromPrefs();
        if (result){
            val sh=getSharedPreferences("DATA_PATH", MODE_PRIVATE)

            val uriPath=sh.getString("PATH","")

            contentResolver.takePersistableUriPermission(Uri.parse(uriPath),Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (uriPath!=null){

                val fileDoc=DocumentFile.fromTreeUri(applicationContext, Uri.parse(uriPath))

                for (file:DocumentFile in fileDoc!!.listFiles()){
                    //   Toast.makeText(this, file.name,Toast.LENGTH_LONG).show()
                    if (!file.name!!.endsWith(".nomedia")) {
                       // if(!file.name!!.endsWith(".jpg") || !file.name!!.endsWith(".png") || !file.name!!.endsWith(".jpeg"))

                        val modelClass=ModelClass(file.name!!,file.uri.toString())

                        statusList.add(modelClass)}


                }
                //   Toast.makeText(this,statusList.toString(),Toast.LENGTH_SHORT).show()


                setupRecyclerView(statusList)
            }

        }

        else{
            getFolderPermission()
        }
        fragmentList=arrayListOf(
           PhotoFragment(),
            VideoFragment()
        )

        tabLayout=findViewById(R.id.tablayout)


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.frameLayout, PhotoFragment())
                .commit()
        }
//supportActionBar!!.title="All Status"

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getFolderPermission() {

        val storageManager=application.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val intent=
            storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
            val targetDirectory="Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
            var uri=intent.getParcelableExtra<Uri>("andorid.provider.extra.INITIAL_URI") as? Uri
            var scheme=uri.toString()
            scheme=scheme.replace("/root/","/tree/")
            scheme+="%3A$targetDirectory"
            uri=Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia/document/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses")
            intent.putExtra("android.provider.extra.INITIAL_URI",uri)
            intent.putExtra("android.content.extra.SHOW_ADVANCED",true)
            startActivityForResult(intent,1234)


        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode== RESULT_OK){
            val treeUri=data?.data
val sharedPreferences=getSharedPreferences("DATA_PATH", MODE_PRIVATE)
            val myedit=sharedPreferences.edit()
            myedit.putString("PATH",treeUri.toString())
            myedit.apply()

            if (treeUri!=null){
                contentResolver.takePersistableUriPermission(treeUri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val fileDoc=DocumentFile.fromTreeUri(applicationContext, treeUri)
                for (file:DocumentFile in fileDoc!!.listFiles()){
                    if (!file.name!!.endsWith(".nomedia")) {
                        val modelClass=ModelClass(file.name!!,file.uri.toString())

                        statusList.add(modelClass)}


                }
                setupRecyclerView(statusList)
            }




        }
    }

    private fun setupRecyclerView(statusList: ArrayList<ModelClass>) {
statusAdapter=applicationContext.let {
    StatusAdapter(it,statusList){
        selectedStatusItem:ModelClass->listItemClicked(selectedStatusItem)
    }
}!!
     rvStatusList.apply {
         setHasFixedSize(true)
         layoutManager=StaggeredGridLayoutManager(3,LinearLayoutManager.VERTICAL)
         adapter=statusAdapter
     }
    }

    private fun listItemClicked(selectedStatusItem: ModelClass) {
val dialog=Dialog(this@MainActivity)
        dialog.setContentView(R.layout.custom_dialog)

        val imageDialog:ImageView=dialog.findViewById(R.id.iv_status_dialog)
        val cardVideo:CardView=dialog.findViewById(R.id.cv_videocard)
        val iconImage:ImageView=dialog.findViewById(R.id.iv_video_icon)
        if (selectedStatusItem.fileUri.endsWith(".mp4")){
            cardVideo.visibility= View.VISIBLE
            iconImage.visibility= View.VISIBLE
        }
        else{
            cardVideo.visibility=View.INVISIBLE
            iconImage.visibility= View.INVISIBLE
        }
        Glide.with(applicationContext).load(Uri.parse(selectedStatusItem.fileUri)).into(imageDialog)
        dialog.show()

      //  Toast.makeText(applicationContext,selectedStatusItem.fileUri,Toast.LENGTH_SHORT).show()

        val btDownload=dialog.findViewById<Button>(R.id.bt_download)
        btDownload.setOnClickListener{
            dialog.dismiss()
            saveFile(selectedStatusItem)
        }
    }

    private fun saveFile(status: ModelClass) {
if (status.fileUri.endsWith(".mp4")){

    val inputStream=contentResolver.openInputStream(Uri.parse(status.fileUri))
    val fileName="${System.currentTimeMillis()}.mp4"
    try {
        val values=ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
        values.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4")
        values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOCUMENTS+"/Videos")
        val uri=contentResolver.insert(
            MediaStore.Files.getContentUri("external"),values
        )
       val outputStream:OutputStream=uri?.let{contentResolver.openOutputStream(it)}!!
        if (inputStream!=null){
        outputStream.write(inputStream.readBytes())}
        outputStream.close()
        Toast.makeText(applicationContext,"Video Saved",Toast.LENGTH_SHORT).show()

    }
    catch (e:IOException){
        Toast.makeText(applicationContext,"Failed",Toast.LENGTH_SHORT).show()

    }

}
        else{
            val bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(status.fileUri))
      val fileName="${System.currentTimeMillis()}.jpg"
    var fos:OutputStream?=null
    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
        contentResolver.also {resolver->
            val contentValues=ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
                put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
            }
            val imageUri:Uri?=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
fos=imageUri?.let { resolver.openOutputStream(it) }
        }
    }
    else{
val imagesDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image=File(imagesDir,fileName)
        fos=FileOutputStream(image)
    }
fos?.use {
    bitmap.compress(Bitmap.CompressFormat.JPEG,100,it)
    Toast.makeText(applicationContext,"Image Saved",Toast.LENGTH_SHORT).show()
}
        }
    }

    private fun showFragment(position: Int) {
        val fragment = fragmentList[position]
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    private fun replaceFragement(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
    private fun readDataFromPrefs(): Boolean {
        val sh=getSharedPreferences("DATA_PATH", MODE_PRIVATE)
        val uriPath=sh.getString("PATH","")
        if (uriPath!=null){
            if (uriPath.isEmpty()){
                return false
            }

        }
        return true

    }
}