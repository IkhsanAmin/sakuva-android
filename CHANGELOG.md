# Changelog

Seluruh perubahan penting pada proyek **Sakuva** dicatat di dokumen ini.
Format mengacu pada [Keep a Changelog](https://keepachangelog.com/id-ID/1.1.0/) dan penomoran versi mengikuti [Semantic Versioning](https://semver.org/lang/id/).

## [1.1.0] - 2026-11-22

### Added
- **Real-Time Currency Formatting**: Implementasi `CurrencyVisualTransformation` dan helper `sanitizeAmountInput` untuk pemformatan ribuan dan desimal otomatis secara real-time saat pengguna mengetik nominal (`IDR`, `JPY`, `USD`, `SGD`, `MYR`, `EUR`) dengan batas aman 13 digit bilangan bulat.
- **Soft Structuralism Design System**: Implementasi palet warna *Cool Silver / Deep Slate*, tipografi `Plus Jakarta Sans`, angka tabular (`tnum`), dan komponen `DoubleBezelCard` di seluruh UI.
- **Dashboard Overhaul**: Penggantian radar chart dengan `ExpenseLineChart` interaktif (4 filter waktu + touch tooltip), penambahan `TopCategoriesCard` (Top 4 + Lainnya), dan *Inline Backup Reminder Banner*.
- **History Overhaul**: Pengelompokan harian dalam `DoubleBezelCard`, Top Bar ringkas dengan `MonthYearPicker`, pencarian lintas bulan, dan `TransactionDetailSheet` (Catat Lagi, Edit, Hapus).
- **Daily Reminder**: Notifikasi pengingat harian menggunakan `WorkManager` dan integrasi `TimePicker` di pengaturan.
- **Transaction Shortcuts**: Penambahan pintasan `+ Kelola Kategori` langsung dari layar editor transaksi.
- **Reset Semua Data**: Fitur di Manajemen Data yang melakukan wipe data atomik beserta *re-seed* kategori & dompet bawaan melalui `DataResetManager.resetAllData()`.
- **Auto-Prompt Notifikasi**: Permintaan izin `POST_NOTIFICATIONS` otomatis pada peluncuran pertama aplikasi (Android 13+) dengan pengaktifan harian reminder jika diizinkan.
- **Atribusi Copyright**: Penambahan footer copyright (`Sakuva · © 2026` / `Oleh Ikhsan Noor Amin`) di layar **Pengaturan**.

### Fixed
- **Status Bar Edge-to-Edge**: Sinkronisasi warna ikon status bar dengan tema aplikasi (Light/Dark) di `MainActivity`.
- **Splash Screen Tema**: Preferensi tema aplikasi (`ThemeMode`) disinkronkan ke OS via `UiModeManager.setApplicationNightMode` (Android 12+), dan ikon splash screen kini membedakan aset mode Terang (Deep Slate `#1E293B` + Emerald `#10B981`) dan mode Gelap (putih `#F8FAFC` + Emerald `#10B981`) dengan latar `#F6F7F9` / `#0F172A`.
- **Backup Logic**: Syarat tampil banner backup diperketat menggunakan `firstLaunchTimestamp` dan pengecekan `transactionCount > 0` (hanya tampil jika sudah ada data dan aplikasi berumur > 7 hari).
- **Settings Layout**: Perbaikan overlap status bar saat scroll dan perbaikan jarak antara teks label dengan komponen `Switch`.
- **Localization**: Menghapus teks hardcoded "Never" pada status backup, diganti dengan string terlokalisasi melalui `AppStrings` di layer `@Composable` ("Terakhir dicadangkan: Belum pernah" / "Last backed up: Never"); pemetaan nama kategori bawaan ke bahasa Inggris; perbaikan label tema bahasa Inggris menjadi `"System"`.
- **Screen Orientation Lock (Portrait)**: Penguncian orientasi vertikal dipindahkan dari catatan umum menjadi pengaturan tersendiri — `android:screenOrientation="portrait"` di `AndroidManifest.xml` dan `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT` di `MainActivity`.
- **CSV Format**: Nominal angka kini diekspor sebagai angka mentah tanpa pemisah ribuan agar lebih mudah diolah di Excel/Sheets.
- **App Icon**: Konfigurasi Adaptive Launcher Icon sesuai standar Android modern.
- **Adaptive Launcher Icon**: Memperbesar skala vektor foreground pada `@mipmap/ic_launcher` dan `@mipmap/ic_launcher_round` agar logo 'S' mengisi area aman (*safe zone* 66dp) launcher Android secara proporsional.
- **History Filter Reset**: Memperbaiki tombol `"Hapus filter"` pada layar **Riwayat** agar mereset seluruh filter aktif (dompet, kategori, tipe transaksi, dan pencarian) sekaligus mengembalikan daftar transaksi ke kondisi default.

## [1.0.0] - 2026-11-22

### Added
- Blueprint arsitektur Pragmatic MVVM + Clean Architecture dan konfigurasi `libs.versions.toml`.
- Skema database Room (`wallets`, `categories`, `transactions`) dengan relasi `ForeignKey.RESTRICT`, soft-delete (`isArchived`), dan pre-populate 6 dompet serta 10 kategori bawaan.
- Utilitas `MoneyFormatter` untuk representasi nominal `Long` ($\times 100$) lintas mata uang.
- Fitur Backup & Restore lokal (`.json`) via `BackupJsonManager` dengan validasi integritas dan transaksi atomik.
- Fitur Export Laporan (`.csv`) via `CsvExportManager` dengan standar RFC 4180 dan UTF-8 BOM.
- Modul UI Pengaturan lengkap dengan konfirmasi restore destruktif.

### Fixed
- Peningkatan stabilitas startup aplikasi dengan perbaikan delegasi `Context` pada `ProvideAppLocale`.
- Pembersihan antarmuka Dashboard: FAB diseragamkan sebagai satu titik masuk tunggal.
