package com.charles.virtualpet.fishtank.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.charles.virtualpet.fishtank.data.feedback.BugReport
import com.charles.virtualpet.fishtank.data.feedback.BugReportRepo
import com.charles.virtualpet.fishtank.data.feedback.CreateIssueRequest
import com.charles.virtualpet.fishtank.data.feedback.DiagnosticsHelper
import com.charles.virtualpet.fishtank.data.feedback.GithubClient
import com.charles.virtualpet.fishtank.data.feedback.GithubComment
import com.charles.virtualpet.fishtank.data.feedback.GithubIssue
import com.charles.virtualpet.fishtank.data.feedback.PostCommentRequest
import com.charles.virtualpet.fishtank.data.feedback.UploadAssetRequest
import com.charles.virtualpet.fishtank.ui.theme.PastelGreen
import com.charles.virtualpet.fishtank.ui.theme.PastelPink
import com.charles.virtualpet.fishtank.ui.theme.PastelPurple
import com.charles.virtualpet.fishtank.ui.theme.PastelYellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun uriToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("Cannot open image")
    val bytes = inputStream.use { it.readBytes() }
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

@Composable
fun FeedbackSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bugReportRepo = remember { BugReportRepo(context) }
    val reports by bugReportRepo.bugReports.collectAsState(initial = emptyList())
    val sortedReports = remember(reports) { reports.sortedByDescending { it.number } }

    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<BugReport?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PastelPink, PastelPurple)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "💬",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Column {
                        Text(
                            text = "Support & Feedback",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Report bugs and track feedback",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Button(
                    onClick = { showReportDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Report a Problem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (sortedReports.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Submitted Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    sortedReports.forEach { report ->
                        ReportRow(
                            report = report,
                            onClick = { selectedReport = report }
                        )
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { title, description, includeDiagnostics, name, email, imageUri ->
                scope.launch {
                    submitReport(
                        context = context,
                        title = title,
                        description = description,
                        includeDiagnostics = includeDiagnostics,
                        name = name,
                        email = email,
                        imageUri = imageUri,
                        repo = bugReportRepo
                    )
                    showReportDialog = false
                }
            }
        )
    }

    selectedReport?.let { report ->
        IssueDetailDialog(
            report = report,
            repo = bugReportRepo,
            onDismiss = { selectedReport = null }
        )
    }
}

@Composable
private fun ReportRow(
    report: BugReport,
    onClick: () -> Unit
) {
    val isOpen = report.status.equals("open", ignoreCase = true)
    val statusColor = if (isOpen) PastelGreen else PastelPink

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title.replace("[Feedback] ", ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "#${report.number} - ${report.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = report.status.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Boolean, String, String, Uri?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var includeDiagnostics by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isConfigured = remember { GithubClient.isConfigured }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    if (isSubmitting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Submitting Report...") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text("Please wait while your report is being submitted.")
                }
            },
            confirmButton = {}
        )
        return
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                "Report a Problem",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Your report will be submitted to this app's GitHub issue tracker. Do not include passwords, private keys, medical information, financial information, or anything you do not want visible to the repository maintainers. If this repository is public, your report may be publicly visible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!isConfigured) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "GitHub configuration is missing. Please set github.api.token, github.repo.owner, and github.repo.name in local.properties.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Subject *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 8
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = includeDiagnostics,
                        onCheckedChange = { includeDiagnostics = it }
                    )
                    Text(
                        "Include phone/app diagnostics",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") }
                    ) {
                        Text(if (imageUri == null) "Attach Screenshot" else "Change Image")
                    }
                    if (imageUri != null) {
                        TextButton(
                            onClick = { imageUri = null }
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "Screenshots may contain private information",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        errorMessage = "Title and description are required."
                        return@Button
                    }
                    if (!isConfigured) {
                        errorMessage = "GitHub config is missing. Cannot submit."
                        return@Button
                    }
                    isSubmitting = true
                    onSubmit(title, description, includeDiagnostics, name, email, imageUri)
                },
                enabled = !isSubmitting && title.isNotBlank() && description.isNotBlank() && isConfigured
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private suspend fun submitReport(
    context: Context,
    title: String,
    description: String,
    includeDiagnostics: Boolean,
    name: String,
    email: String,
    imageUri: Uri?,
    repo: BugReportRepo
) {
    withContext(Dispatchers.IO) {
        val api = GithubClient.api
        val owner = GithubClient.owner
        val repoName = GithubClient.repo
        val assetsDir = GithubClient.assetsDir

        var imageUrl: String? = null
        if (imageUri != null) {
            try {
                val base64Content = uriToBase64(context, imageUri)
                val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                val random = (1000..9999).random()
                val filename = "issue-$timestamp-${random}.png"
                val uploadRequest = UploadAssetRequest(
                    message = "Upload screenshot for issue",
                    content = base64Content
                )
                val uploadResponse = api.uploadAsset(owner, repoName, assetsDir, filename, uploadRequest)
                if (uploadResponse.isSuccessful) {
                    imageUrl = uploadResponse.body()?.content?.downloadUrl
                }
            } catch (_: Exception) { }
        }

        val diagnostics = if (includeDiagnostics) {
            "\n" + DiagnosticsHelper.collectDiagnostics(context)
        } else ""

        val contactInfo = buildString {
            appendLine("## Contact Info")
            appendLine()
            appendLine("- Name: ${name.ifBlank { "Not provided" }}")
            appendLine("- Email: ${email.ifBlank { "Not provided" }}")
        }

        val attachmentSection = if (imageUrl != null) {
            "\n## Attachment\n\n![Screenshot]($imageUrl)\n"
        } else ""

        val body = buildString {
            appendLine("## Description")
            appendLine()
            appendLine(description)
            appendLine()
            appendLine(contactInfo)
            append(attachmentSection)
            append(diagnostics)
        }

        val issueTitle = "[Feedback] $title"
        val request = CreateIssueRequest(title = issueTitle, body = body)
        val response = api.createIssue(owner, repoName, request)

        if (response.isSuccessful) {
            val issue = response.body()!!
            val bugReport = BugReport(
                number = issue.number,
                title = issue.title,
                status = issue.state,
                createdAt = issue.createdAt.take(10),
                htmlUrl = issue.htmlUrl
            )
            repo.saveBugReport(bugReport)
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw Exception("Failed to create issue: ${response.code()} $errorBody")
        }
    }
}

@Composable
fun IssueDetailDialog(
    report: BugReport,
    repo: BugReportRepo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var issue by remember { mutableStateOf<GithubIssue?>(null) }
    var comments by remember { mutableStateOf<List<GithubComment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }
    var replyImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPostingReply by remember { mutableStateOf(false) }

    val replyImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        replyImageUri = uri
    }

    val isConfigured = remember { GithubClient.isConfigured }

    LaunchedEffect(report.number) {
        if (!isConfigured) {
            isLoading = false
            errorMessage = "GitHub config is missing."
            return@LaunchedEffect
        }
        try {
            withContext(Dispatchers.IO) {
                val api = GithubClient.api
                val owner = GithubClient.owner
                val repoName = GithubClient.repo

                val issueResponse = api.getIssue(owner, repoName, report.number)
                if (issueResponse.isSuccessful) {
                    issue = issueResponse.body()
                    val updatedReport = BugReport(
                        number = report.number,
                        title = report.title,
                        status = issueResponse.body()?.state ?: report.status,
                        createdAt = report.createdAt,
                        htmlUrl = report.htmlUrl
                    )
                    repo.saveBugReport(updatedReport)
                }

                val commentsResponse = api.getComments(owner, repoName, report.number)
                if (commentsResponse.isSuccessful) {
                    comments = commentsResponse.body() ?: emptyList()
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to fetch issue: ${e.message}"
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = report.title.replace("[Feedback] ", ""),
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                issue?.let { iss ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (iss.state == "open") PastelGreen.copy(alpha = 0.3f)
                                    else PastelPink.copy(alpha = 0.3f)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = iss.state.uppercase(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(
                            text = "#${iss.number}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    iss.body?.let { bodyText ->
                        Text(
                            text = bodyText.take(500),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (comments.isNotEmpty()) {
                    Text(
                        text = "Comments (${comments.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    comments.forEach { comment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = comment.user.login,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = comment.createdAt.take(10),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.body.take(1000),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Write a reply...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 4,
                    enabled = isConfigured
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { replyImagePicker.launch("image/*") },
                        enabled = isConfigured
                    ) {
                        Text(
                            if (replyImageUri == null) "Attach Image" else "Change Image",
                            fontSize = 12.sp
                        )
                    }
                    if (replyImageUri != null) {
                        TextButton(onClick = { replyImageUri = null }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }

                replyImageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Attachment",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                Button(
                    onClick = {
                        if (replyText.isBlank()) return@Button
                        isPostingReply = true
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    val api = GithubClient.api
                                    val owner = GithubClient.owner
                                    val repoName = GithubClient.repo
                                    val assetsDir = GithubClient.assetsDir

                                    var imageUrl: String? = null
                                    replyImageUri?.let { uri ->
                                        try {
                                            val base64Content = uriToBase64(context, uri)
                                            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                                            val random = (1000..9999).random()
                                            val filename = "comment-$timestamp-${random}.png"
                                            val uploadRequest = UploadAssetRequest(
                                                message = "Upload comment attachment",
                                                content = base64Content
                                            )
                                            val uploadResponse = api.uploadAsset(owner, repoName, assetsDir, filename, uploadRequest)
                                            if (uploadResponse.isSuccessful) {
                                                imageUrl = uploadResponse.body()?.content?.downloadUrl
                                            }
                                        } catch (_: Exception) { }
                                    }

                                    val body = buildString {
                                        appendLine("## Reply")
                                        appendLine()
                                        appendLine(replyText)
                                        if (imageUrl != null) {
                                            appendLine()
                                            appendLine("## Attachment")
                                            appendLine()
                                            appendLine("![Screenshot]($imageUrl)")
                                        }
                                    }

                                    val request = PostCommentRequest(body = body)
                                    val response = api.postComment(owner, repoName, report.number, request)
                                    if (response.isSuccessful) {
                                        val updatedComments = api.getComments(owner, repoName, report.number)
                                        if (updatedComments.isSuccessful) {
                                            withContext(Dispatchers.Main) {
                                                comments = updatedComments.body() ?: emptyList()
                                                replyText = ""
                                                replyImageUri = null
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) { }
                            isPostingReply = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = replyText.isNotBlank() && !isPostingReply && isConfigured,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isPostingReply) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Post Reply")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val api = GithubClient.api
                            val owner = GithubClient.owner
                            val repoName = GithubClient.repo
                            val issueResponse = api.getIssue(owner, repoName, report.number)
                            if (issueResponse.isSuccessful && issueResponse.body() != null) {
                                val iss = issueResponse.body()!!
                                val updated = BugReport(
                                    number = report.number,
                                    title = report.title,
                                    status = iss.state,
                                    createdAt = report.createdAt,
                                    htmlUrl = report.htmlUrl
                                )
                                repo.saveBugReport(updated)
                            }
                            val commentsResponse = api.getComments(owner, repoName, report.number)
                            if (commentsResponse.isSuccessful) {
                                comments = commentsResponse.body() ?: emptyList()
                            }
                        } catch (_: Exception) { }
                    }
                }
            }) {
                Text("Refresh")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
