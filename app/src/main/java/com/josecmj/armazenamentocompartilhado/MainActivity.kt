package com.josecmj.armazenamentocompartilhado

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.josecmj.armazenamentocompartilhado.databinding.ActivityMainBinding

private const val READ_EXTERNAL_STORAGE_REQUEST = 1

private const val DELETE_PERMISSION_REQUEST = 2


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val imageAdapter : ImageAdapter = ImageAdapter(this::delete)

    private val fileListViewModel by viewModels<ImageViewModel>{
        ImageViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, 2)
        }
        recyclerView.adapter = imageAdapter



        if (!haveStoragePermission()) {
            requestPermission()
        } else {
            showImages()
        }
    }

    private fun showImages() {
        fileListViewModel.filesLiveData.observe(this, {
            it?.let {
                imageAdapter.submitList(it as MutableList<MyImage>)
                imageAdapter.getItemCount();
            }

        })
    }

    private fun haveStoragePermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


    private fun requestPermission() {
        if (!haveStoragePermission()) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(
                this,
                permissions,
                READ_EXTERNAL_STORAGE_REQUEST
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun delete(uri: Uri){
        try {
            contentResolver.delete(
                uri,
                "${MediaStore.Images.Media._ID} = ?",
                arrayOf(ContentUris.parseId(uri).toString())
            )
        } catch (securityException: SecurityException) {
            val recoverSecException =
                securityException as? RecoverableSecurityException ?: throw
                securityException
            val intentSender = recoverSecException.userAction.actionIntent.intentSender
            startIntentSenderForResult(intentSender, DELETE_PERMISSION_REQUEST, null, 0, 0, 0, null)
        }

        fileListViewModel.filesLiveData.postValue(fileListViewModel.dataSource.getImages())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImages()
                } else {
                    // If we weren't granted the permission, check to see if we should show
                    // rationale for the permission.
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    if (showRationale) {
                        Log.d("ZECA","SEM PERMISSAO")
                    } else {
                        goToSettings()
                    }
                }
                return
            }
        }
    }

    private fun goToSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }
}