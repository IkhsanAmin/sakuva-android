# Technical Specification (SPEC) — Sakuva

## 1. Tech Stack & Konfigurasi Build
- **Bahasa:** Kotlin 2.0.21[cite: 3]
- **UI Framework:** Jetpack Compose (Material 3), Tipografi `Plus Jakarta Sans`[cite: 3]
- **SDK Target:** `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`[cite: 3]
- **Arsitektur:** Pragmatic MVVM + Clean Architecture (Unidirectional Data Flow / UDF)[cite: 3]
- **Dependency Injection:** Hilt + KSP[cite: 3]
- **Database Lokal:** Room (`exportSchema = true`, lokasi skema di `app/schemas`)[cite: 3]
- **Preferensi Lokal:** Preferences DataStore (Tema, Bahasa, Mata Uang, Status & Pengingat Backup, Jadwal Pengingat Harian, `firstLaunchTimestamp`)[cite: 10][cite: 3]
- **Background Work:** WorkManager (`PeriodicWorkRequest` untuk notifikasi pengingat catat harian)
- **Navigasi:** Navigation Compose (Type-safe `@Serializable` routes)[cite: 3]
- **Serialisasi:** `kotlinx.serialization` (JSON Backup & Navigation)[cite: 3]
- **Async & Reaktivitas:** Kotlin Coroutines & Flow (`StateFlow`)[cite: 3]
- **File I/O:** Android Storage Access Framework (`ActivityResultContracts.CreateDocument` & `OpenDocument`, tanpa izin `WRITE_EXTERNAL_STORAGE`)[cite: 3]
- **Testing:** JUnit4, Google Truth, Turbine, `kotlinx-coroutines-test`[cite: 3]

## 2. Arsitektur & Struktur Paket
Package dasar: `com.sakuva.app`[cite: 3]

```text
com.sakuva.app/
├── core/           # MoneyFormatter, DateTimeUtils, LocaleManager, DesignSystem
├── di/             # Hilt Modules (DatabaseModule, DataStoreModule, RepositoryModule)
├── data/
│   ├── local/      # Room (SakuvaDatabase, Entities, DAOs, Callback) & DataStore
│   ├── io/         # BackupJsonManager, CsvExportManager
│   ├── mapper/     # Konversi Entity <-> Domain Model
│   └── repository/ # Implementasi Repository
├── domain/
│   ├── model/      # Domain models (Wallet, Category, Transaction, ExpenseTrendPoint, CategoryExpenseSummary)
│   ├── repository/ # Interface Repository
│   └── usecase/    # Logika bisnis lintas-entitas & agregasi
├── worker/         # DailyReminderWorker & NotificationHelper
└── feature/ (ui/)  # Layar Compose (splash, dashboard, history, wallet, transaction, settings) & shared components (DoubleBezelCard)
```[cite: 3]

## 3. Skema Database (Room) & Preferensi DataStore

### 3.1 Entitas Utama & Indexing
- **`wallets` (`WalletEntity`):**
  `id` (PK, autoGenerate), `name`, `type` (`CASH`, `BANK`, `EWALLET`), `initialBalance` (`Long` $\times 100$), `iconKey`, `colorHex`, `isArchived` (`Boolean`), `createdAt` (`epochMillis`)[cite: 3].
- **`categories` (`CategoryEntity`):**
  `id` (PK, autoGenerate), `name`, `type` (`INCOME`, `EXPENSE`), `iconKey`, `colorHex`, `isDefault`, `isArchived`[cite: 3].
- **`transactions` (`TransactionEntity`):**
  `id` (PK, autoGenerate), `amount` (`Long` $\times 100$, selalu positif $> 0$), `type` (`INCOME`, `EXPENSE`, `TRANSFER`), `walletId` (FK $\rightarrow$ `wallets.id`), `toWalletId` (FK $\rightarrow$ `wallets.id`, nullable), `categoryId` (FK $\rightarrow$ `categories.id`, nullable), `dateEpochDay` (`Long` dari `LocalDate.toEpochDay()`), `note`, `createdAt` (`epochMillis`)[cite: 3].
- **Wajib `@Index`:**
  - Single-column index pada `walletId`, `toWalletId`, dan `categoryId` (mencegah *full table scan* saat agregasi saldo & menghilangkan Room FK compile warning)[cite: 3].
  - Composite index `(type, dateEpochDay)` untuk mempercepat agregasi Dashboard dan Grafik Pengeluaran[cite: 3].
  - Single-column index pada `dateEpochDay` untuk filter rentang tanggal pada Riwayat dan Export CSV[cite: 3].

### 3.2 Aturan Integritas, Query & Pencarian
- **Single Source of Truth untuk Saldo:** Saldo dompet dihitung lewat query SQL:
  `initialBalance + SUM(INCOME) - SUM(EXPENSE) - SUM(TRANSFER keluar) + SUM(TRANSFER masuk)`[cite: 3].
- **Representasi Tanggal vs Waktu:** `dateEpochDay` digunakan untuk pengelompokan periode kalender (Harian/Mingguan/Bulanan/Tahunan) agar tidak bergeser saat pengguna berpindah zona waktu, sedangkan `createdAt` (`epochMillis`) digunakan sebagai *tie-breaker* urutan kronologis dalam hari yang sama[cite: 3].
- **Soft-Delete & Foreign Key:** Seluruh foreign key menggunakan `ForeignKey.RESTRICT`. Penghapusan dompet/kategori hanya mengubah `isArchived = 1`[cite: 3].
- **Synchronous Pre-populate:** `DatabaseCallback.onCreate(db: SupportSQLiteDatabase)` menyisipkan 6 dompet bawaan (`Tunai`, `BRI`, `BNI`, `BSI`, `BCA`, `E-Wallet`) dan 10 kategori bawaan secara sinkron via `db.execSQL(...)` di dalam transaksi `onCreate` untuk mencegah *race condition* state kosong saat peluncuran pertama[cite: 3].
- **Strategi Pemuatan Riwayat:** Mode *Idle* memuat transaksi berdasarkan bulan aktif (`YearMonth`), sedangkan Mode *Search* mendukung kueri terindeks lintas bulan dan rentang `dateEpochDay` kustom[cite: 3].

### 3.3 Preferensi DataStore
- `themeMode`: `SYSTEM` (default) | `LIGHT` | `DARK`
- `languageCode`: `id` (default) | `en`
- `currencyCode`: ISO 3-letter code (`IDR` default, `USD`, `SGD`, `MYR`, `EUR`, `JPY`)
- `lastBackupTimestamp`: `Long` (default `0L`, diperbarui setiap kali Export JSON berhasil)
- `isBackupReminderEnabled`: `Boolean` (default `true`)
- `isDailyReminderEnabled`: `Boolean` (default `false`)
- `reminderHour`: `Int` (default `20`), `reminderMinute`: `Int` (default `0`)
- `firstLaunchTimestamp`: `Long` (default `0L`, dicatat saat peluncuran pertama; digunakan sebagai referensi waktu banner backup dan logika usia aplikasi)
- `hasPromptedNotificationPermission`: `Boolean` (default `false`, menandai apakah izin `POST_NOTIFICATIONS` sudah pernah dipinta agar auto-prompt hanya terjadi sekali)

## 4. Aturan Teknis & Spesifikasi Fitur Inti

### 4.1 Representasi & Parsing Nominal (`MoneyFormatter`)
- Seluruh nominal uang disimpan sebagai `Long` terskala $\times 100$ (contoh: `IDR 15.000` = `1_500_000L`, `USD 15.50` = `1_550L`) untuk menghindari *floating-point rounding error*[cite: 3].
- **Format Tampilan UI (Strict ISO Code Only):** Menggunakan `BigDecimal.valueOf(amountScaled, 2)` dengan pemformatan grup ribuan sesuai locale, **WAJIB** menggunakan kode mata uang 3 huruf (`IDR`, `USD`, `SGD`, `MYR`, `EUR`, `JPY`) dan **DILARANG** menampilkan simbol mata uang (`Rp`, `$`, `€`, `¥`) di seluruh UI (contoh: `IDR 150.000` atau `-IDR 35.000`). Mata uang nol-desimal (`IDR`, `JPY`, `KRW`, `VND`) diformat dengan 0 angka di belakang koma[cite: 3].
- **Kontrak Parsing Input:** Parsing string input wajib menerima `currencyCode` aktif secara eksplisit untuk menentukan *grouping separator* dan *decimal separator* sesuai locale mata uang tersebut (menghapus pemisah ribuan terlebih dahulu sebelum mengonversi pemisah desimal ke titik). Untuk mata uang nol-desimal (`IDR`, `JPY`, `KRW`, `VND`), input pecahan ditolak atau dibulatkan ke kelipatan `100L`.
- **Pemformatan Input Real-Time (`CurrencyVisualTransformation`):** Seluruh input nominal (`TransactionEditorScreen`, `WalletEditorScreen`) menggunakan `CurrencyVisualTransformation` untuk memformat pemisah ribuan dan desimal secara otomatis saat pengguna mengetik. `sanitizeAmountInput` memastikan input hanya berisi digit dan atasi satu pemisah desimal sesuai aturan mata uang (`IDR`/`JPY` nol-desimal; `USD`/`SGD`/`MYR`/`EUR` maksimal 2 desimal), dengan batas aman **13 digit** bilangan bulat (`9.999.999.999.999`).

### 4.2 Dashboard: Banner Backup, `ExpenseLineChart` & `TopCategoriesCard`
- **Inline Top Banner Pengingat Backup:**
  - Ditampilkan di urutan paling atas `DashboardScreen` (tepat di atas kartu Total Saldo) **hanya jika SEMUA syarat berikut terpenuhi**:
    a. `isBackupReminderEnabled == true`
    b. `firstLaunchTimestamp > 0L` (aplikasi telah dibuka setidaknya sekali)
    c. **Dan salah satu:**
       - `lastBackupTimestamp == 0L` (belum pernah dibuat cadangan)
       - **ATAU** `System.currentTimeMillis() - lastBackupTimestamp > 7 hari` (cadangan terakhir > 7 hari)
    d. Total transaksi di database `> 0` (sudah ada data yang tercatat)
    e. Belum di-dismiss pada sesi aktif (dengan `dismissBackupReminder()`).
  - Logika Referensi Waktu: `referenceTime = if (lastBackupTimestamp > 0) lastBackupTimestamp else firstLaunchTimestamp`.
  - Memiliki tombol primer **"Backup Sekarang"** (memicu alur Export JSON & memperbarui `lastBackupTimestamp` saat sukses) serta tombol tutup (`Icons.Rounded.Close`, `contentDescription = "Tutup"`) di pojok kanan atas banner untuk men-dismiss sesi aktif tanpa mengubah timestamp cadangan[cite: 3].
- **Grafik Tren Pengeluaran (`ExpenseLineChart`):**
  - Dibangun menggunakan Custom Compose `Canvas` dan `rememberTextMeasurer()` tanpa library chart eksternal[cite: 3].
  - Memfilter **HANYA transaksi bertipe `EXPENSE`** (gabungan semua dompet; abaikan `INCOME` dan `TRANSFER`).
  - Mendukung 4 filter periode waktu:
    - **Harian:** Agregasi pengeluaran hari ini dalam 6–8 blok waktu (`00:00`, `04:00`, `08:00`, `12:00`, `16:00`, `20:00`).
    - **Mingguan:** 7 titik (`Sen` – `Min` minggu berjalan; hari tanpa transaksi bernilai `0`).
    - **Bulanan:** Titik per tanggal (`1` s.d. akhir bulan), dengan label sumbu X jarang (`1`, `7`, `14`, `21`, `28`) agar tidak bertumpuk.
    - **Tahunan:** 12 titik (`Jan` – `Des` tahun berjalan).
  - Visual & Interaksi: Kurva mulus (*cubic bezier*), area gradasi vertikal lembut di bawah kurva menuju transparan, garis grid horizontal tipis putus-putus, serta dukungan sentuh/geser (`pointerInput`) yang menampilkan indikator titik aktif dan *tooltip* waktu + nominal (`tnum`).
- **Rekap Kategori Terbesar (`TopCategoriesCard`):**
  - Ditempatkan tepat di bawah `ExpenseLineChart` dan **tersinkronisasi dengan filter periode yang sama**.
  - Mengurutkan kategori `EXPENSE` dari nominal terbesar ke terkecil: menampilkan maksimal **Top 3–4 kategori**, dan menggabungkan sisanya (jika $> 4$ kategori) menjadi satu baris **`Lainnya`**.
  - Setiap baris menampilkan Ikon + Nama Kategori di kiri, Persentase + Nominal (`tnum`) di kanan, serta `LinearProgressIndicator` tipis (`height = 6.dp`, rounded) di bawahnya.
  - **Sembunyikan otomatis** seluruh blok ini apabila total pengeluaran pada periode aktif bernilai `0`.

### 4.3 History Screen ("The Quiet Ledger")
- **Mode Diam (*Idle State*):**
  - **Top Bar:** Judul `"Riwayat"` di kiri dan pemilih bulan interaktif **`< Nov 2026 >`** di pojok kanan atas. Panah `<` dan `>` menggeser bulan; mengetuk teks **`Nov 2026`** membuka dialog **`MonthYearPicker`** (pemilih tahun `< 2026 >` + grid 12 bulan `Jan`–`Des`).
  - **Search Bar:** Satu bilah pencarian kapsul di bawah Top Bar. Seluruh `FilterChip` tersembunyi pada mode ini.
  - **Daftar Transaksi:** Transaksi pada bulan terpilih dikelompokkan per tanggal (`HARI INI · 22 NOV`, `KEMARIN · 21 NOV`, dst.) dan dibungkus dalam **satu `DoubleBezelCard` per hari** (antar transaksi dipisahkan `HorizontalDivider` tipis). Baris daftar **tidak** menampilkan tombol titik tiga (`⋮`), ikon edit, atau ikon hapus.
- **Mode Pencarian Aktif (*Focused / Active Search*):**
  - Saat bilah pencarian diketuk (*focused*), Top Bar tersembunyi sementara (`AnimatedVisibility`), bilah pencarian naik ke atas disertai tombol **`Batal`** (mereset query, menghapus filter, dan mengembalikan Top Bar).
  - Muncul baris `FilterChip` berupa *dropdown* di bawah bilah cari: **Dompet**, **Kategori**, dan **Tipe** — masing-masing menampilkan label default `Semua dompet` / `Semua kategori` / `Semua tipe` saat tidak ada filter aktif (opsi `null` = tanpa filter). Pilihan langsung disimpan ke `HistoryViewModel` (`HistoryFilters.walletId` / `categoryId` / `type`), bukan di *local state* Composable.
  - **Rentang Tanggal:** Opsi tanggal membuka Material 3 **`DateRangePicker`** untuk rentang kustom (mis. `1 Jan 2026` – `3 Mar 2026`). Saat aktif, muncul baris `Rentang: <mulai> - <selesai>` dan rentang menggantikan filter bulan.
  - **Hapus filter:** Saat ada filter aktif, tombol **`Hapus filter`** tampil dan mereset **seluruh** filter (dompet, kategori, tipe, pencarian, dan rentang tanggal) kembali ke default melalui `HistoryViewModel.clearFilters()`, sehingga daftar transaksi pulih ke tampilan bulan berjalan.
  - Pencarian berjalan **lintas seluruh bulan** (kecuali dibatasi `DateRangePicker`) dengan logika kombinasi (*AND*) antara chip aktif dan query teks—mencocokkan `note`, `category.name`, serta string tanggal terformat (mis. `"17 Agu"`, `"22 Nov 2026"`, `"Okt 2026"`). Setiap baris hasil menampilkan tanggal lengkap pada subjudulnya.
- **Tap-to-Inspect Bottom Sheet (`TransactionDetailSheet`):**
  - Mengetuk baris transaksi membuka `ModalBottomSheet` yang menampilkan detail lengkap (ikon, kategori, nominal `tnum`, catatan penuh, dompet asal/tujuan, tanggal & jam) serta **3 tombol aksi jelas**:
    1. **`Catat Lagi`** (tombol utama lebar penuh): Membuka `TransactionEditorScreen` mode *Create* dengan nominal, tipe, kategori, dompet, dan catatan terisi otomatis dari transaksi tersebut, namun tanggal/waktu disetel ke **saat ini (`now`)**.
    2. **`Edit`**: Membuka `TransactionEditorScreen` mode *Edit*.
    3. **`Hapus`**: Menampilkan dialog konfirmasi sebelum menghapus transaksi.

### 4.4 Settings Screen & Notifikasi Pengingat Harian
- **Kontrol UI Modern (dalam `DoubleBezelCard`):**
  - **Tema:** `SingleChoiceSegmentedButtonRow` dengan 3 opsi — `Sistem` / `Terang` / `Gelap` (Bahasa Indonesia) atau `System` / `Light` / `Dark` (Bahasa Inggris).
  - **Bahasa:** `SingleChoiceSegmentedButtonRow` (`Indonesia`, `English`).
  - **Mata Uang:** `LazyRow` berisi `FilterChip` untuk kode ISO 3 huruf (`IDR`, `USD`, `SGD`, `MYR`, `EUR`, `JPY`).
- **Sinkronisasi Tema Splash Screen per-Aplikasi** *(ditempatkan setelah daftar kontrol agar urutan list tidak terpotong)*:
  - Preferensi `ThemeMode` aplikasi disinkronkan ke sistem operasi melalui `UiModeManager.setApplicationNightMode(...)` pada Android 12+ (`MODE_NIGHT_NO` untuk `LIGHT`, `MODE_NIGHT_YES` untuk `DARK`, `MODE_NIGHT_AUTO` untuk `SYSTEM`), dipanggil saat preferensi tema dibaca di `MainActivity` dan setiap kali pengguna mengubah tema di `SettingsViewModel`.
  - Latar *System Splash Screen* mengikuti tema: `#F6F7F9` (Terang) dan `#0F172A` (Gelap).
  - Ikon simbol splash dibedakan: `@drawable/ic_splash_foreground` (simbol `'S'` Deep Slate `#1E293B` + panah Emerald `#10B981`) untuk mode terang, dan `@drawable/ic_splash_foreground_dark` (simbol `'S'` putih `#F8FAFC` + panah Emerald `#10B981`) untuk mode gelap. Launcher icon home screen tetap menggunakan `@mipmap/ic_launcher` / `ic_launcher_round` dan tidak berubah.
- **Auto-Prompt Izin Notifikasi Saat Peluncuran Pertama:**
  - Saat aplikasi dibuka untuk pertama kali (`hasPromptedNotificationPermission == false`) dan berjalan di Android 13+ (`Build.VERSION.SDK_INT >= 33`), secara otomatis meminta izin `Manifest.permission.POST_NOTIFICATIONS`.
  - Jika pengguna mengizinkan, `dailyReminderEnabled` diaktifkan secara otomatis dan `WorkManager` menjadwalkan pengingat harian.
  - Setelah prompt ditampilkan (diizinkan atau ditolak), `hasPromptedNotificationPermission` diatur ke `true` agar prompt tidak muncul kembali.
- **Notifikasi Pengingat Catat Harian:**
  - `Switch` pengingat harian: saat diaktifkan, memeriksa/meminta izin `Manifest.permission.POST_NOTIFICATIONS` pada Android 13+ (`Build.VERSION.SDK_INT >= 33`), menampilkan baris pengaturan jam yang membuka Material 3 **`TimePicker`**, dan menjadwalkan notifikasi harian melalui `WorkManager`.
- **Manajemen Data:**
  - `Switch` untuk mengaktifkan/menonaktifkan banner pengingat backup mingguan di Dashboard.
  - **Status backup reaktif** ditampilkan di layer `@Composable` melalui `LocalStrings.current` (mengikuti perubahan bahasa tanpa restart): `"Terakhir dicadangkan: Belum pernah"` / `"Last backed up: Never"` saat `lastBackupTimestamp == 0L`, atau `"Terakhir dicadangkan: [dd MMM yyyy HH:mm]"` / `"Last backed up: [dd MMM yyyy HH:mm]"` jika sudah pernah dicadangkan.
  - **Reset Semua Data:**
    - Baris `SettingsActionRow` bertanda *destructive* (ikon `Delete`, warna aksen merah) di bagian Manajemen Data.
    - Mengetuk baris ini membuka **dialog konfirmasi dua langkah** (`ResetDataDialog` → `DestructiveConfirmDialog`) dengan judul dan pesan peringatan yang menjelaskan bahwa seluruh transaksi, dompet, dan kategori akan dihapus.
    - Setelah konfirmasi, `DataResetManager.resetAllData()` mengeksekusi penghapusan seluruh data secara **atomik** di dalam `database.withTransaction`: hapus `transactions` → `categories` → `wallets`, lalu *re-seed* **6 dompet bawaan** (`Tunai`, `BRI`, `BNI`, `BSI`, `BCA`, `E-Wallet`) dan **10 kategori bawaan** sesuai bahasa aktif, serta mereset `lastBackupTimestamp` ke `0L`.
- **Footer Copyright & Atribusi** *(di bawah kartu **Tentang**)*:
  - Garis pemisah horizontal tipis (`0.5.dp`, `onSurfaceVariant` opasitas `0.16`), diikuti teks brand `Sakuva · © 2026` (font `labelSmall`, `11.sp`, `FontWeight.Medium`, *letter-spacing* `0.08.em`, opasitas `0.82`) dan teks kreator `Oleh Ikhsan Noor Amin` (Bahasa Indonesia) / `By Ikhsan Noor Amin` (Bahasa Inggris) (font `labelSmall`, `11.sp`, `FontWeight.Normal`, *letter-spacing* `0.03.em`, opasitas `0.54`).
  - Seluruh teks footer terlokalisasi melalui `AppStrings` (`appCopyrightBrand`, `appCopyrightCreator`) sehingga otomatis mengikuti pilihan bahasa pengguna.

### 4.5 Backup & Restore JSON (`BackupJsonManager`)
- Menggunakan DTO terpisah (`SakuvaBackupPayload` dengan `schemaVersion = 1`) agar kompatibel terhadap migrasi skema Room di masa depan[cite: 3].
- **Validasi Untrusted Input sebelum `withTransaction`:**
  - **Batas Ukuran Berkas:** Tolak berkas yang melebihi `15 MB` sebelum membaca seluruh isi ke memori untuk mencegah `OutOfMemoryError`[cite: 3].
  - **Validasi Skema & Integritas ID:** Pastikan `schemaVersion <= CURRENT_SCHEMA_VERSION`, `wallets` tidak kosong, dan seluruh `id > 0` bersifat unik per entitas[cite: 3].
  - **Validasi Format Visual:** Pastikan `colorHex` cocok dengan pola `#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})` dan `name`/`iconKey` tidak kosong agar UI tidak *crash* setelah pemulihan[cite: 3].
  - **Invarian Domain Transaksi:** Pastikan `amount > 0`, seluruh `walletId`/`toWalletId`/`categoryId` merujuk ke ID yang valid di dalam payload; jika `type == TRANSFER` maka `toWalletId != null && walletId != toWalletId && categoryId == null`; jika `type` adalah `INCOME` atau `EXPENSE` maka `categoryId != null && toWalletId == null`[cite: 3].
- **Atomic Execution:** Penimpaan data dijalankan di dalam `database.withTransaction`: hapus `transactions` $\rightarrow$ `categories` $\rightarrow$ `wallets`, lalu sisipkan `wallets` $\rightarrow$ `categories` $\rightarrow$ `transactions`[cite: 3]. Saat Export JSON sukses, perbarui `lastBackupTimestamp` di DataStore.

### 4.6 Export CSV (`CsvExportManager`)
- **Format Nominal Mentah (Strict Raw Numeric):** Kolom `Nominal` ditulis sebagai angka murni tanpa kode/simbol mata uang dan tanpa pemisah ribuan (contoh: `150000` atau `150000.50`, bukan `IDR 150.000` atau `Rp 150.000`).
- **Placeholder Sel Kosong:** Setiap kolom opsional yang kosong/null (seperti `Catatan` kosong atau `Dompet Tujuan` pada transaksi non-transfer) **WAJIB** ditulis sebagai karakter **`-`** (bukan string kosong atau `"null"`).
- **Proteksi CSV Formula Injection:** Setiap nilai teks (`Kategori`, `Sumber Dana`, `Dompet Tujuan`, `Catatan`) yang diawali karakter `=`, `+`, `@`, `\t`, atau `\r` (serta `-` yang diikuti karakter selain akhir sel kosong) wajib diawali petik tunggal (`'`) sebelum di-escape agar tidak dieksekusi sebagai formula di Excel atau Google Sheets[cite: 3].
- **Standar Format:** Mematuhi RFC 4180 (membungkus field dengan tanda kutip ganda jika mengandung koma, kutip ganda, atau baris baru, serta menggandakan kutip internal) dan menyisipkan UTF-8 BOM (`0xEF, 0xBB, 0xBF`) di awal berkas[cite: 3].
- **Kolom:** `Tanggal,Tipe,Kategori,Sumber Dana,Dompet Tujuan,Nominal,Catatan`[cite: 3].

### 4.7 Pintasan Kategori di `TransactionEditorScreen`
- Di dalam pemilih kategori (*Category Selector*) pada `TransactionEditorScreen`, sediakan item/tombol aksi **`+ Kelola Kategori`** yang langsung menavigasi pengguna ke `CategoryManagementScreen`.

### 4.8 Baseline Keamanan Platform, Edge-to-Edge Status Bar & Rilis
- **Sinkronisasi Android Status Bar (Edge-to-Edge):**
  - Di `MainActivity.kt`, panggil `enableEdgeToEdge(...)` di dalam `LaunchedEffect(darkTheme)` menggunakan `SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)` saat tema gelap aktif dan `SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)` saat tema terang aktif—mengikuti state `themeMode` Sakuva (`Sistem`, `Terang`, `Gelap`), bukan sekadar `isSystemInDarkTheme()`.
  - Hapus seluruh *override* `window.statusBarColor = ...` di `Theme.kt` agar tidak menimpa `SystemBarStyle`.
  - Seluruh *top-level screen* (`DashboardScreen`, `HistoryScreen`, `WalletScreen`, `SettingsScreen`, `TransactionEditorScreen`) wajib menerapkan `WindowInsets.statusBars` (`statusBarsPadding()` atau `Scaffold` `innerPadding`) agar konten tidak pernah naik menyatu dengan bar baterai/jam.
- **Konfigurasi Manifest:** `android:allowBackup="false"` dan `android:dataExtractionRules` dikonfigurasi untuk mencegah pencadangan otomatis database SQLite tanpa enkripsi via ADB/cloud; seluruh komponen internal memakai `android:exported="false"`[cite: 3].
- **Penguncian Orientasi Vertikal (Portrait):**
  - `android:screenOrientation="portrait"` dideklarasikan pada `<activity>` di `AndroidManifest.xml` agar aplikasi selalu terkunci pada orientasi potret di seluruh perangkat.
  - `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT` juga diatur secara programatik di `MainActivity` (`requestedOrientation`) sebagai lapisan pertahanan kedua yang tidak dapat ditimpa oleh konfigurasi sistem atau perubahan orientasi dinamis.
  - Tujuan: menjaga konsistensi tata letak UI (terutama grafik dan kartu `DoubleBezelCard`) serta menghindari *layout shift* saat perangkat diputar.
- **Kebersihan Log & Error:** Tidak ada pencatatan data finansial (nominal, saldo, catatan) ke `Logcat`; seluruh *exception* pada layer I/O dan database dipetakan menjadi pesan kesalahan ramah pengguna tanpa membocorkan detail internal[cite: 3].
- **Build Rilis:** `isMinifyEnabled = true` dan `isShrinkResources = true` aktif pada varian `release`, dengan seluruh versi dependensi terkunci di `gradle/libs.versions.toml`[cite: 3].

## 5. Design System ("Soft Structuralism") & Aksesibilitas

### 5.1 Palet Warna & Tipografi
- **Light Mode (*Cool Silver*):** `background = #F6F7F9`, `surface = #FFFFFF`, `surfaceContainer = #EEF1F5`, `onSurface = #111827`, `onSurfaceVariant = #6B7280`, `primary = #1E293B`.
- **Dark Mode (*Deep Slate*):** `background = #0F172A`, `surface = #1E293B`, `surfaceContainer = #334155`, `onSurface = #F8FAFC`, `onSurfaceVariant = #94A3B8`, `primary = #38BDF8` (atau `#E2E8F0`).
- **Tipografi:** `Plus Jakarta Sans` di seluruh aplikasi, dengan `fontFeatureSettings = "tnum"` (*tabular numbers*) pada semua angka nominal, persentase, dan label grafik.
- **Komponen Kartu Standar (`DoubleBezelCard`):** Container luar `RoundedCornerShape(20.dp)` dengan border `1.dp` (`outlineVariant` opasitas rendah), padding `4.dp`, latar `surfaceContainerLow`; container dalam `RoundedCornerShape(16.dp)`, latar `surface`, padding konten `16.dp`.

### 5.2 Modularitas, Responsivitas & WCAG
- **Lokalisasi Nama Kategori Bawaan (ID/EN):** Seluruh 10 kategori bawaan (6 `EXPENSE` + 4 `INCOME`) memiliki nama yang dilokalisasi sesuai bahasa aktif saat *re-seed* berlangsung (baik saat `DatabaseCallback.onCreate` maupun setelah `DataResetManager.resetAllData()`). Kategori kustom buatan pengguna **tidak** diubah atau diterjemahkan oleh mekanisme lokalisasi ini — hanya nama kategori ber-flag `isDefault` yang mengikuti `AppStrings`.
- **Pemisahan Layer UI:** Setiap fitur memisahkan `Route/Container` (terhubung ke ViewModel & SAF launcher) dari `Screen/Component` murni (*stateless*, mudah di-preview dan diuji)[cite: 3].
- **Modularitas:** Menjaga file dan komponen tetap ringkas dengan memisahkan sub-komponen dan dialog ke file tersendiri (`*Components.kt`, `*Dialogs.kt`)[cite: 3].
- **Responsivitas:** Konten utama dibatasi `widthIn(max = 640.dp)` dan dipusatkan untuk kenyamanan baca pada layar lebar/tablet[cite: 3].
- **Aksesibilitas (WCAG / TalkBack):**
  - Target sentuh minimum `48.dp`–`56.dp`[cite: 3].
  - Penggunaan `semantics { heading() }`, `Role.Button`, `Role.RadioButton`, `selectableGroup()`, serta `contentDescription` eksplisit untuk indikator loading[cite: 3].
  - Umpan balik status (seperti `Snackbar` sukses/error dan dialog destruktif) selalu menggabungkan ikon + teks + warna, tidak mengandalkan warna saja[cite: 3].

## 6. Strategi Pengujian (Unit Test)
Prioritas pengujian otomatis pada layer `core`, `domain`, dan `data/io`[cite: 3]:
- **`MoneyFormatterTest`:** Format multi-mata uang ISO 3 huruf tanpa simbol (`IDR`, `USD`, `JPY`) dan parsing input string sesuai locale mata uang ke `Long` $\times 100$[cite: 3].
- **`GetWalletBalancesUseCaseTest`:** Akurasi saldo awal, pemasukan, pengeluaran, dan transfer antar-dompet[cite: 3].
- **`GetExpenseAnalyticsUseCaseTest`:** Agregasi `ExpenseLineChart` (4 periode waktu, khusus `EXPENSE`) serta pengurutan Top 3–4 kategori + penggabungan `Lainnya`.
- **`BackupJsonManagerTest`:** Validasi skema JSON, batas ukuran file, format `colorHex`, dan penolakan invarian transaksi yang rusak[cite: 3].
- **`CsvExportManagerTest`:** Format angka mentah tanpa pemisah/simbol, penggantian sel kosong menjadi `-`, escaping RFC 4180, dan sanitasi prefix formula pada `buildCsvString`[cite: 3].
- **`SettingsViewModelTest`:** Pengujian alur Reset Semua Data (pemanggilan `DataResetManager.resetAllData()` dan pengisian ulang *re-seed*), status backup reaktif (`lastBackupAt` `null` → `notBackedUp`, `non-null` → format tanggal), serta pengaturan preferensi (`setThemeMode`, `setLanguage`, `setCurrency`, `setDailyReminderEnabled`, `setBackupReminderEnabled`, `setDailyReminderTime`).
- **`CurrencyVisualTransformationTest`:** Pengujian pemformatan dan `OffsetMapping` (`originalToTransformed` & `transformedToOriginal`) untuk seluruh mata uang (`IDR`, `JPY`, `USD`, `SGD`, `MYR`, `EUR`), batas pemotongan 13 digit bilangan bulat, dan batas 2 digit desimal pada `sanitizeAmountInput`.