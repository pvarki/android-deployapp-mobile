package com.pvarki.deployapp.data.model

class AllProductsInstructionFiles(
    val files: Map<String, List<FileItem>>
)

data class FileItem(
    val title: String,
    val filename: String,
    val data: String
)

