package com.example.moneymanagement.view.fragment_setting

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moneymanagement.MyApp
import com.example.moneymanagement.R
import com.example.moneymanagement.adapter.CategoryAdapter
import com.example.moneymanagement.data.model.Category
import com.example.moneymanagement.data.model.TransactionDatabase
import com.example.moneymanagement.data.model.TransactionType
import com.example.moneymanagement.data.repository.TransactionRepository
import com.example.moneymanagement.databinding.FragmentTypeManagementBinding
import com.example.moneymanagement.utils.UserManager
import com.example.moneymanagement.viewmodel.TransactionViewModel
import com.example.moneymanagement.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TypeManagementFragment : Fragment() {

    private var _binding: FragmentTypeManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    private var currentType: TransactionType = TransactionType.EXPENSE
    private var allCategories: List<Category> = emptyList()

    private var selectedIcon: String = ""
    private var selectedImageUri: Uri? = null

    private var currentIvSelectedImage: ImageView? = null
    private var currentTvSelectedIcon: TextView? = null
    private var currentLayoutDefaultIcon: LinearLayout? = null
    private var currentIvEditOverlay: ImageView? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            showPermissionDeniedDialog()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                selectedIcon = ""
                updateImagePreview(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTypeManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupViewModel() {
        val app: MyApp = requireActivity() as MyApp
        val factory = TransactionViewModelFactory(app.repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        val userId = UserManager.getCurrentUserId()
        if (!userId.isNullOrEmpty()) {
            viewModel.setUserId(userId)
            android.util.Log.d("TypeManagement", "Set userId: $userId")
        } else {
            Toast.makeText(requireContext(), "Không thể xác định người dùng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                showCategoryDialog(category)
            },
            onCategoryLongClick = { category ->
                showDeleteConfirmation(category)
                true
            }
        )

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentType = TransactionType.EXPENSE
                        filterCategories()
                    }
                    1 -> {
                        currentType = TransactionType.INCOME
                        filterCategories()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabAdd.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeData() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            allCategories = categories
            filterCategories()
        }
    }

    private fun filterCategories() {
        val filteredCategories = allCategories.filter { it.type == currentType }
        categoryAdapter.submitList(filteredCategories)

        if (filteredCategories.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvCategories.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvCategories.visibility = View.VISIBLE
        }
    }

    private fun showAddCategoryDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)

        val btnClose = dialogView.findViewById<ImageView>(R.id.btn_close)
        val frameIconPicker = dialogView.findViewById<FrameLayout>(R.id.frame_icon_picker)
        val tvSelectedIcon = dialogView.findViewById<TextView>(R.id.tv_selected_icon)
        val ivSelectedImage = dialogView.findViewById<ImageView>(R.id.iv_selected_image)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.et_category_name)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_description)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

        selectedIcon = ""
        selectedImageUri = null

        currentTvSelectedIcon = tvSelectedIcon
        currentIvSelectedImage = ivSelectedImage

        ivSelectedImage?.visibility = View.GONE
        tvSelectedIcon?.visibility = View.GONE

        val alertDialog = dialog.setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        frameIconPicker.setOnClickListener {
            showIconPickerBottomSheet()
        }

        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSave.setOnClickListener {
            val categoryName = etCategoryName.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (categoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên nhóm phân loại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedIcon.isEmpty() && selectedImageUri == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn icon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveCategory(categoryName, description)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showIconPickerBottomSheet() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_icon_picker, null)

        val btnChooseEmoji = bottomSheetView.findViewById<Button>(R.id.btn_choose_emoji)
        val btnUploadImage = bottomSheetView.findViewById<Button>(R.id.btn_upload_image)

        btnChooseEmoji.setOnClickListener {
            showEmojiPicker()
            bottomSheet.dismiss()
        }

        btnUploadImage.setOnClickListener {
            checkPermissionAndPickImage()
            bottomSheet.dismiss()
        }

        bottomSheet.setContentView(bottomSheetView)
        bottomSheet.show()
    }

    private fun showEmojiPicker() {
        val emojis = listOf(
            "🍕", "🍔", "🍟", "🍗", "🍖", "🌭", "🥙", "🌮", "🌯", "🥗",
            "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🍤", "🍙", "🍚", "🍘",
            "🍥", "🥮", "🍢", "🍡", "🍧", "🍨", "🍦", "🥧", "🧁", "🍰",
            "🎂", "🍮", "🍭", "🍬", "🍫", "🍿", "🍩", "🍪", "🌰", "🥜",
            "☕", "🍵", "🧃", "🥤", "🧋", "🍶", "🍺", "🍻", "🥂", "🍷",
            "🏠", "🚗", "✈️", "🎓", "💊", "🎮", "📱", "👕", "⚽", "🎬"
        )

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Chọn emoji")

        builder.setItems(emojis.toTypedArray()) { dialog, which ->
            selectedIcon = emojis[which]
            selectedImageUri = null

            currentTvSelectedIcon?.text = selectedIcon
            currentTvSelectedIcon?.visibility = View.VISIBLE
            currentLayoutDefaultIcon?.visibility = View.GONE
            currentIvSelectedImage?.visibility = View.GONE
            currentIvEditOverlay?.visibility = View.GONE
        }

        builder.show()
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionRationaleDialog(permission)
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun updateImagePreview(uri: Uri) {
        try {
            currentIvSelectedImage?.setImageURI(uri)
            currentIvSelectedImage?.visibility = View.VISIBLE
            currentTvSelectedIcon?.visibility = View.GONE
            currentLayoutDefaultIcon?.visibility = View.GONE
            currentIvEditOverlay?.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Không thể tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionRationaleDialog(permission: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cần quyền truy cập")
            .setMessage("Ứng dụng cần quyền truy cập thư viện ảnh để bạn có thể chọn icon cho nhóm phân loại.")
            .setPositiveButton("Cho phép") { _, _ ->
                permissionLauncher.launch(permission)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quyền bị từ chối")
            .setMessage("Bạn đã từ chối quyền truy cập thư viện ảnh. Vui lòng vào Cài đặt để bật quyền này.")
            .setPositiveButton("Mở Cài đặt") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun saveCategory(name: String, description: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = UserManager.getCurrentUserId()
                if (userId.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Không thể xác định người dùng", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val iconPath: String = when {
                    selectedImageUri != null -> {
                        val savedPath = saveImageToInternalStorage(selectedImageUri!!, name)
                        android.util.Log.d("TypeManagement", "Saved image to: $savedPath")
                        savedPath ?: run {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }
                    }
                    selectedIcon.isNotEmpty() -> {
                        android.util.Log.d("TypeManagement", "Using emoji: $selectedIcon")
                        selectedIcon
                    }
                    else -> {
                        android.util.Log.d("TypeManagement", "Using default emoji")
                        "📁"
                    }
                }

                val currentCategories = viewModel.allCategories.value ?: emptyList()
                android.util.Log.d("TypeManagement", "Current categories count: ${currentCategories.size}")
                android.util.Log.d("TypeManagement", "Current max ID: ${currentCategories.maxOfOrNull { it.id }}")

                val nextId = viewModel.getNextCategoryId()
                android.util.Log.d("TypeManagement", "Next ID will be: $nextId")


                val newCategory = Category(
                    id = nextId,
                    type_name = name,
                    icon = iconPath,
                    type = currentType,
                    userId = userId
                )

                android.util.Log.d("TypeManagement", "Saving category: $newCategory")

                viewModel.insertCategory(newCategory)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Đã thêm: $name với icon", Toast.LENGTH_SHORT).show()
                    selectedIcon = ""
                    selectedImageUri = null
                }
            } catch (e: Exception) {
                android.util.Log.e("TypeManagement", "Error saving category", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveImageToInternalStorage(uri: Uri, categoryName: String): String? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }

            if (bitmap == null) {
                return null
            }

            val directory = File(requireContext().filesDir, "category_icons")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val timestamp = System.currentTimeMillis()
            val sanitizedName = categoryName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val filename = "category_${timestamp}_${sanitizedName}.jpg"
            val file = File(directory, filename)

            val maxSize = 512
            val resizedBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
                val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()

            file.absolutePath

        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getNextCategoryId(): Int {
        return (allCategories.maxOfOrNull { it.id } ?: 0) + 1
    }

    private fun showCategoryDialog(category: Category) {
        Toast.makeText(requireContext(), "Chỉnh sửa: ${category.type_name}", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation(category: Category) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa thể loại")
            .setMessage("Bạn có chắc chắn muốn xóa \"${category.type_name}\"?\n\nLưu ý: Các giao dịch liên quan sẽ không bị xóa.")
            .setPositiveButton("Xóa") { _, _ ->
                deleteImageFile(category.icon)

                viewModel.deleteCategory(category)

                Toast.makeText(
                    requireContext(),
                    "Đã xóa ${category.type_name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteImageFile(iconPath: String) {
        try {
            if (iconPath.startsWith("/")) {
                val file = File(iconPath)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TypeManagementFragment()
    }
}