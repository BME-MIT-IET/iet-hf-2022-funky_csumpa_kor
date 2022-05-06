package com.remenyo.papertrader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remenyo.papertrader.Order
import com.remenyo.papertrader.shortTimeFromUnixTimestamp
import com.remenyo.papertrader.ui.theme.colorScheme
import com.remenyo.papertrader.ui.theme.loss
import com.remenyo.papertrader.ui.theme.profit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(
    o: Order,
    marketClose: ((id: String) -> Unit)? = null,
    cancelOrder: ((id: String) -> Unit)? = null,
    currentMarketSellPrice: Double? = null
) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(10.dp, 8.dp)) {
            Column {
                if (o.opened()) Text("Opened at: ${shortTimeFromUnixTimestamp(ts = o.openTS)}")
                Text("BEP: ${o.bep}$")
                if (o.sep != 0.0) Text("SEP: ${o.sep}$")
                Text("Multi: ${o.multi}x")
                if (!o.cancelled && o.opened()) {
                    if (o.closed()) Text(
                        "P/L: %.2f".format(o.pnl()), color = if (o.pnl() < 0) loss else profit
                    )
                    else if (currentMarketSellPrice != null) Text(
                        "UP/L: %.2f".format(o.upnl(currentMarketSellPrice)),
                        color = if (o.upnl(currentMarketSellPrice) < 0) loss else profit
                    )
                }
                if (o.trailing()) {
                    Text("Trailing price: ${o.trail}$")
                }
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                if (o.cancelled) {
                    Text("Cancelled", color = colorScheme.error)
                } else {
                    if (o.opened()) {
                        if (o.closed()) {
                            Text("Closed at ${shortTimeFromUnixTimestamp(ts = o.closeTS)}")
                        } else {
                            Text("Opened", color = colorScheme.primary)
                        }
                    } else {
                        Text("Not yet opened")
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (!o.cancelled) {
                    if (!o.closed()) {
                        if (o.opened()) {
                            if (marketClose != null) Button(onClick = { marketClose(o.id) }) {
                                Text(
                                    "Market close"
                                )
                            }
                        } else if (cancelOrder != null) Button(onClick = { cancelOrder(o.id) }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}