package club.hiraeth.flareenough.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import club.hiraeth.flareenough.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.about_version),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(R.string.about_tagline), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.about_licence),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.about_author),
                style = MaterialTheme.typography.bodyMedium,
            )

            InfoCard(
                title = stringResource(R.string.about_dedication_title),
                body = stringResource(R.string.about_dedication_body),
                centerBody = true,
            )
            ConnectCard()
            InfoCard(
                title = stringResource(R.string.about_not_medical_title),
                body = stringResource(R.string.about_not_medical_body),
            )
            InfoCard(
                title = stringResource(R.string.about_privacy_title),
                body = stringResource(R.string.about_privacy_body),
            )
        }
    }
}

/**
 * A calm card of ways to reach the maker and, optionally, to support the app. Every
 * row opens in the browser or the relevant app through a system intent, so the app
 * itself still needs no internet permission and touches no network. A missing
 * handler is caught, so a tap never crashes.
 */
@Composable
private fun ConnectCard() {
    val context = LocalContext.current
    val email = stringResource(R.string.about_contact)
    val threadsUrl = stringResource(R.string.about_threads_url)
    val kofiUrl = stringResource(R.string.about_kofi_url)
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.about_connect_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.about_connect_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            LinkRow(
                icon = Icons.Filled.Email,
                title = stringResource(R.string.about_email_label),
                detail = email,
                onClick = { sendEmail(context, email) },
            )
            LinkRow(
                icon = Icons.Filled.AlternateEmail,
                title = stringResource(R.string.about_threads_label),
                detail = stringResource(R.string.about_threads_handle),
                onClick = { openUri(context, threadsUrl) },
            )
            LinkRow(
                icon = Icons.Filled.Favorite,
                title = stringResource(R.string.about_kofi_label),
                detail = stringResource(R.string.about_kofi_handle),
                onClick = { openUri(context, kofiUrl) },
            )
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Open a web link in the browser or matching app. No network access in this app. */
private fun openUri(context: Context, uri: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    }
}

/** Open the mail app to write to an address, with nothing sent by the app itself. */
private fun sendEmail(context: Context, address: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address")))
    }
}

@Composable
private fun InfoCard(title: String, body: String, centerBody: Boolean = false) {
    // Each line of the body is its own short paragraph with a little space around it.
    // The dedication (centerBody) is centred and balanced, the way a book dedication
    // is set, so no line is left with a stray word hanging off the end.
    val paragraphs = body.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = if (centerBody) Alignment.CenterHorizontally else Alignment.Start,
            ) {
                paragraphs.forEach { paragraph ->
                    if (centerBody) {
                        BalancedText(text = paragraph, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text(paragraph, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/**
 * Text that wraps in balanced lines, so a two line phrase splits into two roughly
 * equal halves instead of a long line with one stray word beneath it. It measures
 * the text, then narrows the width to the tightest point that still uses the same
 * number of lines, which pushes the wrap to a balanced spot. Centred, this reads
 * like a book dedication. It re-measures with the layout, so it stays right at any
 * width and font scale. Used only for the short, static dedication lines.
 */
@Composable
private fun BalancedText(text: String, style: TextStyle) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val fullWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
        val targetWidthPx = remember(text, style, fullWidthPx) {
            if (fullWidthPx <= 0) {
                fullWidthPx
            } else {
                val naturalLines = measurer
                    .measure(text, style, constraints = Constraints(maxWidth = fullWidthPx))
                    .lineCount
                if (naturalLines <= 1) {
                    fullWidthPx
                } else {
                    // Smallest width that still fits in `naturalLines` lines without a
                    // word overflowing. Binary search over the available width.
                    var lo = 1
                    var hi = fullWidthPx
                    while (lo < hi) {
                        val mid = (lo + hi) / 2
                        val result = measurer.measure(
                            text,
                            style,
                            constraints = Constraints(maxWidth = mid),
                        )
                        if (result.lineCount <= naturalLines && !result.hasVisualOverflow) {
                            hi = mid
                        } else {
                            lo = mid + 1
                        }
                    }
                    lo
                }
            }
        }
        val targetWidthDp = with(LocalDensity.current) { targetWidthPx.toDp() }
        Text(
            text = text,
            style = style,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(targetWidthDp),
        )
    }
}
