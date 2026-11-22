# Sakuva â€” Finance Tracker (Offline-First)

Sakuva adalah aplikasi pencatat keuangan pribadi berbasis Android yang mengutamakan privasi (offline-first) dengan antarmuka **Soft Structuralism** yang bersih dan modern.

## Fitur Utama
- **Multi-Wallet**: Kelola saldo di berbagai dompet (Tunai, Bank, E-Wallet).
- **Analitik Visual**: Pantau tren pengeluaran dengan *Expense Line Chart* interaktif.
- **Manajemen Data**: Backup/Restore berbasis JSON, ekspor laporan CSV untuk Excel/Sheets, serta Reset Semua Data.
- **Bilingual (Indonesia & English)**: Seluruh antarmuka tersedia dalam Bahasa Indonesia dan Bahasa Inggris, dapat ditukar langsung dari Pengaturan tanpa restart.
- **Privasi Total**: Data disimpan secara lokal di perangkat pengguna, tidak ada server pihak ketiga.
- **Pengingat Cerdas**: Notifikasi harian untuk mencatat transaksi dan pengingat backup mingguan.

## Spesifikasi Teknis (v1.1)
- **Min SDK**: 26 (Android 8.0)
- **Architecture**: Clean Architecture + MVVM + UDF
- **Stack**: Kotlin 2.0.21, Compose (Material 3), Room, Hilt, WorkManager, DataStore
- **Design System**: Soft Structuralism (Plus Jakarta Sans, Tabular Numbers)

## Cara Build
```powershell
$env:JAVA_HOME="<path-ke-JDK-21>"
.\gradlew.bat assembleDebug
```

## Lisensi
Â© 2026 Ikhsan Noor Amin. Seluruh aset grafis dan kode sumber dilindungi oleh hak cipta.
