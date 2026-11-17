# Habit Tracker

Aplikasi Android pelacak kebiasaan, dibuat dengan **Kotlin + Jetpack Compose (Material 3)** dan
penyimpanan lokal **Room**. Tidak butuh internet dan tidak ada akun — semua data tersimpan di perangkat.

## Fitur

- **Hari ini** — daftar habit yang dijadwalkan, tombol centang sekali tap, ring progres harian,
  dan strip 7 hari terakhir untuk mencatat mundur (backfill) hari yang terlewat.
- **Tiga jenis jadwal**
  - *Harian* — setiap hari.
  - *Hari tertentu* — hanya pada hari yang dipilih (mis. Sen/Rab/Jum).
  - *Fleksibel* — target N kali per minggu, bebas harinya.
- **Streak cerdas** — hari yang memang tidak dijadwalkan tidak memutus streak; hari ini dianggap
  masa tenggang sehingga streak kemarin tidak langsung hilang. Habit fleksibel dihitung per minggu.
- **Detail habit** — streak saat ini & terbaik, konsistensi 30 hari, total tercatat, toggle 7 hari
  terakhir, dan heatmap 16 minggu ala GitHub.
- **Statistik** — ringkasan hari ini, rata-rata 30 hari, hari sempurna, grafik penyelesaian 14 hari,
  dan rekap per habit.
- **Pengingat notifikasi** — per habit bisa diatur jam pengingatnya. Notifikasi hanya muncul pada
  hari yang memang dijadwalkan, dilewati kalau habit sudah dicentang (atau target mingguan habit
  fleksibel sudah tercapai), dan punya aksi **Tandai selesai** langsung dari notifikasi.
- **Arsip & hapus** — habit bisa diarsipkan (riwayat tetap tersimpan) atau dihapus permanen.
- **Maskot chibi (Lottie)** — karakter maskot yang bereaksi pada keadaan: mood berubah mengikuti
  progres harian, streak, dan hasil statistik; muncul sebagai reaksi saat habit dicentang; serta
  memandu saat mengisi form. Hormat pada setelan sistem: kalau animasi dimatikan, maskot tampil
  sebagai gambar diam.
- **Material You** — dynamic color di Android 12+, dukungan light & dark theme.

## Arsitektur

```
domain/      Habit, ScheduleType, StreakCalculator, ReminderPlanner (Kotlin murni, diuji tanpa Android)
data/        HabitRepository + Room (HabitEntity, CompletionEntity, HabitDao, HabitDatabase)
notification/ ReminderScheduler (AlarmManager), ReminderReceiver, BootReceiver, HabitNotifier
ui/mascot/   Mascot (19 sticker Lottie), MascotMoods (aturan mood), MascotView/Burst, reaksi & empty state
ui/          theme/, components/ (ProgressRing, WeekStrip, HabitRow, Heatmap, chart),
             home/, detail/, editor/, stats/, navigation/
```

- Pola **MVVM unidirectional**: ViewModel menggabungkan Flow dari Room menjadi satu `UiState`
  (`stateIn` + `collectAsStateWithLifecycle`), Compose hanya merender state.
- **Tanpa framework DI** — `AppContainer` sederhana di `HabitTrackerApplication` dipakai lewat
  `viewModelFactory`.
- Perhitungan streak/konsistensi ada di `StreakCalculator` sebagai fungsi murni, di-cover unit test.
- Tanggal disimpan sebagai `epochDay` (Long) dan diolah dengan `java.time.LocalDate`.

### Peta mood maskot

Aturan pemilihan mood ada di `MascotMoods` (fungsi murni, diuji unit test):

| Layar | Keadaan | Maskot |
|---|---|---|
| Hari ini | belum punya habit | `waving` |
| Hari ini | tidak ada jadwal | `sleeping` |
| Hari ini | 0% selesai | `low_energy` |
| Hari ini | <50% / ≥50% / 100% | `focused` / `happy` / `celebrating` |
| Reaksi centang | habit biasa / habit terakhir hari itu | `excited` / `goal_achievement` |
| Detail habit | streak ≥21 / ≥7 / ≥1 | `goal_achievement` / `happy_content` / `focused` |
| Detail habit | bukan hari jadwalnya | `recharging` |
| Detail habit | streak 0, konsistensi <30% | `drained` (selain itu `low_energy`) |
| Detail habit | diarsipkan | `locked` |
| Statistik | rata-rata ≥85% / ≥60% / ≥25% / <25% | `goal_achievement` / `happy_content` / `monitoring` / `burned_out` |
| Statistik | belum ada data | `confused` |
| Editor | nama kosong / terisi / error validasi | `thinking` / `happy_writing` / `surprised` |
| Editor | pengingat aktif / nonaktif | `meditating` / `sleeping` |

Aset Lottie ada di `app/src/main/res/raw/` (19 file, 320×320, 30fps, loop 3 detik, PNG ter-embed,
total ~2,2 MB). Menambah mood baru cukup: taruh file di `res/raw`, tambahkan satu entri di enum
`Mascot`, lalu pakai di aturan `MascotMoods`.

### Ikon aplikasi

Ikon launcher dibuat dari maskot `happy_writing`: gambar karakternya diambil dari aset Lottie
(`img_4`, 178×210 PNG) dan dipakai sebagai layer foreground adaptive icon.

- `drawable-nodpi/mascot_writing.png` — gambar karakter
- `drawable/ic_launcher_foreground.xml` — layer-list yang menempatkan maskot 34×40dp di tengah
  kanvas 108dp. Ukuran ini sengaja lebih kecil dari safe zone 66dp karena sebagian launcher
  (ColorOS, misalnya) ikut memperbesar foreground, sehingga maskot tetap utuh saat dimask lingkaran.
- `drawable/ic_launcher_background.xml` — gradien hijau diagonal
- `drawable/ic_launcher_monochrome.xml` — layer themed icon (Android 13+). Siluet chibi tidak
  terbaca saat diratakan jadi satu warna, jadi versi ini memakai glyph centang sederhana.

### Cara kerja pengingat

1. `ReminderPlanner.nextTrigger()` menghitung kapan alarm berikutnya (melewati hari non-jadwal).
2. `ReminderScheduler` memasang satu alarm per habit lewat `AlarmManager.setAndAllowWhileIdle`
   — **inexact**, jadi tidak perlu izin *exact alarm*; sistem boleh menggesernya beberapa menit.
3. `ReminderReceiver` mengecek ulang saat alarm berbunyi (habit masih ada? sudah dicentang?
   target mingguan sudah tercapai?), menampilkan notifikasi, lalu menjadwalkan alarm berikutnya.
4. `HabitTrackerApplication` mengamati daftar habit dan menyinkronkan ulang semua alarm setiap ada
   perubahan; `BootReceiver` memasang ulang setelah reboot atau update aplikasi.

Izin yang dipakai: `POST_NOTIFICATIONS` (diminta saat pengguna menyalakan pengingat pertama kali,
Android 13+) dan `RECEIVE_BOOT_COMPLETED`.

## Menjalankan

```bash
./gradlew :app:assembleDebug          # build APK debug
./gradlew :app:testDebugUnitTest      # unit test logika streak
./gradlew :app:installDebug           # pasang ke perangkat/emulator yang terhubung
```

Atau buka folder ini di Android Studio lalu tekan Run.

Build ID debug memakai suffix `.debug`, jadi versi debug dan release bisa terpasang berdampingan.

## Spesifikasi teknis

| Item | Nilai |
|---|---|
| minSdk / targetSdk / compileSdk | 26 / 35 / 35 |
| Kotlin | 2.0.0 (Compose Compiler plugin) |
| AGP / Gradle | 8.9.1 / 8.11.1 |
| Compose BOM | 2024.09.03 |
| Room | 2.6.1 (KSP) |
| Lottie Compose | 6.5.2 |
| JVM target | 17 |

Database dibuat dengan tiga habit contoh saat pertama kali dipasang; hapus saja lewat layar detail
kalau tidak diperlukan. Skema database versi 2 (versi 1 → 2 menambahkan kolom pengingat) memakai
`Migration` resmi, jadi data lama tidak hilang saat update.
