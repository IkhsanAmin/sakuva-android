package com.sakuva.app.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sakuva.app.core.designsystem.DoubleBezelCard
import com.sakuva.app.core.designsystem.Spacing
import com.sakuva.app.core.locale.LocalStrings
import com.sakuva.app.core.money.MoneyFormatter
import com.sakuva.app.domain.model.Wallet
import com.sakuva.app.feature.common.ContentWidthContainer
import com.sakuva.app.feature.common.EmptyState
import com.sakuva.app.feature.common.LoadingState

@Composable
fun WalletScreen(
    uiState: WalletUiState,
    onAddWallet: () -> Unit,
    onEditWallet: (Wallet) -> Unit,
    onArchiveWallet: (Wallet) -> Unit,
) {
    val strings = LocalStrings.current
    val totalBalance = uiState.wallets.sumOf { it.balance }

    ContentWidthContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Text(
                    text = strings.wallets,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
            item {
                DoubleBezelCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.totalWalletsBalance,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = MoneyFormatter.format(totalBalance, uiState.currencyCode),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            item {
                Button(
                    onClick = onAddWallet,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(text = strings.addWallet)
                }
            }

            if (uiState.isLoading) {
                item {
                    LoadingState(modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            } else if (uiState.wallets.isEmpty()) {
                item {
                    EmptyState(title = strings.noWallets)
                }
            } else {
                items(uiState.wallets, key = { it.wallet.id }) { item ->
                    WalletCard(
                        item = item,
                        currencyCode = uiState.currencyCode,
                        onEdit = { onEditWallet(item.wallet) },
                        onArchive = { onArchiveWallet(item.wallet) },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}
