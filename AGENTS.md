# AGENTS.md — Panduan Pengembangan Sakuva

Dokumen ini menjadi acuan standar kode, arsitektur, keamanan, penyederhanaan kode, dan UI bagi developer maupun AI coding assistant selama mengerjakan repositori **Sakuva**.

## 1. Arsitektur & Konvensi Kode
- **Arsitektur:** Pragmatic MVVM + Clean Architecture dengan *Unidirectional Data Flow* (`StateFlow`).
- **Paket Dasar:** `com.sakuva.app` (`core`, `di`, `data`, `domain`, `feature`).
- **Dependency Injection:** Wajib menggunakan Hilt (`@HiltViewModel`, `@Singleton`, `@Inject`).
- **Async:** Gunakan Kotlin Coroutines & `Flow`. Hindari operasi I/O atau query database di *Main Thread*.

## 2. Aturan Data, Keamanan & Hardening (`security-and-hardening`)

### 2.1 Trust Boundaries & Validasi Input
- **Boundary Eksternal (SAF Restore JSON):** Perlakukan setiap berkas `.json` yang dipilih pengguna sebagai *untrusted input*. Wajib batasi ukuran maksimum **15 MB** via *bounded stream reader*, gunakan parser ketat (`ignoreUnknownKeys = false`, `isLenient = false`), validasi skema, regex `colorHex`, serta seluruh invarian domain sebelum menjalankan `database.withTransaction`.
- **Boundary Eksternal (Export CSV):** Wajib menyertakan **UTF-8 BOM**, mengikuti **RFC 4180**, dan mensanitasi awalan formula (`=`, `+`, `-`, `@`, `\t`, `\r`) dengan tanda petik tunggal (`'`) untuk mencegah *CSV Formula Injection*.
- **Validasi Input Form:** Validasi nominal (wajib positif `> 0`, tidak melampaui `Long.MAX_VALUE`), panjang teks nama/catatan, serta aturan transfer (`walletId != toWalletId`) di layer domain/ViewModel — jangan hanya mengandalkan batasan keyboard UI.

### 2.2 Integritas Database & Presisi Keuangan
- **Presisi Keuangan:** Seluruh nominal uang wajib disimpan sebagai `Long` terskala **×100**. Dilarang menggunakan `Float` atau `Double` untuk perhitungan/penyimpanan uang. Formatting dan parsing wajib melalui `MoneyFormatter`.
- **Tanggal & Waktu:** Gunakan `dateEpochDay: Long` (`LocalDate.toEpochDay()`) untuk pengelompokan periode transaksi agar bebas pergeseran zona waktu, dan `createdAt: Long` (`epochMillis`) untuk urutan kronologis dalam hari yang sama.
- **Parameterized Queries Only:** Seluruh query Room wajib menggunakan parameter terikat (`:param`). Dilarang membangun query SQL menggunakan string concatenation dari input pengguna.
- **Integritas Skema Room:**
  - Saldo dompet dihitung lewat query SQL (bukan kolom saldo statis yang dimutasi manual).
  - Seluruh *foreign key* wajib memakai `ForeignKey.RESTRICT` dan wajib memiliki `@Index` (termasuk kolom filter tanggal).
  - Penghapusan dompet dan kategori menggunakan *soft-delete* (`isArchived = true`).
  - *Pre-populate* data bawaan di `DatabaseCallback.onCreate` wajib dijalankan secara **sinkron** (`db.execSQL`).

### 2.3 Privasi, Logging & Error Handling
- **Zero Financial Data in Logs:** Dilarang mencetak nominal transaksi, saldo dompet, catatan transaksi, atau isi mentah file backup JSON ke `Logcat` (`Log.d`/`Log.e`), terutama pada build rilis.
- **Sanitasi Pesan Error UI:** Jangan pernah menampilkan *stack trace*, pesan exception mentah (`e.message`), nama tabel SQL, atau path internal ke Snackbar/Dialog. Petakan error internal ke pesan bahasa manusia yang aman dan generik.
- **Data Minimization:** Jangan meminta atau menyimpan data identitas sensitif (NIK, nomor rekening asli, email, atau lokasi) yang tidak diperlukan untuk pencatatan keuangan lokal.

### 2.4 Konfigurasi Android, Secrets & Supply Chain
- **AndroidManifest Hardening:** Set `android:allowBackup="false"` (dan atur `android:dataExtractionRules`) agar database keuangan lokal tidak ikut terunggah otomatis ke cloud/ADB di luar mekanisme Backup JSON eksplisit milik pengguna. Pastikan komponen internal menggunakan `android:exported="false"`.
- **Zero Secrets in Git:** Dilarang keras melakukan commit file `.jks`/`.keystore`, password signing, atau API key ke repositori. Simpan kredensial signing hanya di `local.properties` (yang di-gitignore) atau *environment variables* CI.
- **Release & Dependency Hygiene:** Aktifkan R8 (`isMinifyEnabled = true`, `isShrinkResources = true`) untuk build rilis. Kunci seluruh versi dependensi di `gradle/libs.versions.toml`, hindari library pihak ketiga yang tidak terawat, dan audit kerentanan dependensi sebelum rilis.

## 3. Standar UI (`frontend-ui-engineering`)
- **Pemisahan Layer UI:** Pisahkan `*Route` (terhubung ke ViewModel & SAF launcher) dari `*Screen` / komponen presentasi yang *stateless*.
- **Ukuran File:** Jaga file UI tetap ringkas (kurang dari 200 baris per file); pecah sub-komponen ke `*Components.kt` dan dialog ke `*Dialogs.kt`.
- **Design System Tokens:** Gunakan token Material 3 (`MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`) dan skala spasi `4dp`/`8dp`/`12dp`/`16dp`/`24dp`. Dilarang melakukan *hardcode* warna hex atau radius sudut di layar fitur.
- **Responsivitas:** Bungkus konten utama dengan `widthIn(max = 640.dp)` dan pusatkan pada layar lebar/tablet.
- **Aksesibilitas (WCAG / TalkBack):**
  - Area sentuh minimum `48.dp`–`56.dp`.
  - Gunakan `semantics { heading() }`, `Role.Button`, `Role.RadioButton`, `selectableGroup()`, serta `contentDescription` pada indikator *loading*.
  - Status sukses, error, dan aksi destruktif wajib memadukan **ikon + teks + warna** (tidak hanya warna).

## 4. Penyederhanaan Kode (`code-simplification`)
- **Preserve Behavior Exactly:** Saat menyederhanakan kode, jangan mengubah output, *side effect*, *edge case*, maupun perilaku *error handling*. Seluruh unit test lama harus tetap lulus tanpa dimodifikasi.
- **Clarity Over Cleverness:** Utamakan kode yang langsung terbaca daripada kode singkat yang rumit. Hindari *nested ternary* atau rantai operator panjang; gunakan `when`, `if/else` datar, atau variabel perantara dengan nama deskriptif.
- **Guard Clauses & Early Return:** Batasi kedalaman *nesting* maksimal 2–3 level dengan *early return* atau `require(...)` di awal fungsi.
- **Single Responsibility & Balance:** Pecah fungsi yang terlalu panjang (lebih dari 50 baris) menjadi fungsi kecil yang terfokus, tetapi jangan menghapus abstraksi yang dibutuhkan untuk *testability* atau kejelasan konsep domain.
- **Penamaan & Komentar Bermakna:** Hindari nama generik (`data`, `temp`, `val`, `result` tanpa konteks). Tulis komentar hanya untuk menjelaskan **alasan/intent** (*why*), bukan mengulang apa yang sudah jelas tertulis di kode (*what*).
- **Scope Discipline & Clean Diff:**
  - Pisahkan commit/PR *refactoring* dari penambahan fitur baru atau perbaikan bug.
  - Jangan lakukan *drive-by refactor* pada file di luar ruang lingkup tugas saat ini.
  - Hapus *dead code*, variabel tak terpakai, dan *unused imports* setelah memastikan benar-benar tidak digunakan.

## 5. Pengujian
- Setiap perubahan pada `MoneyFormatter`, `UseCase`, `BackupJsonManager`, dan `CsvExportManager` wajib disertai unit test JUnit4 + Google Truth (termasuk *abuse cases* untuk input korup/berbahaya).