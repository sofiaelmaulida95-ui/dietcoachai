package com.example.data.model

data class WorkoutGuideItem(
    val dayOfWeek: String, // Senin, Selasa, Rabu, Kamis, Jumat, Sabtu, Minggu
    val title: String,
    val category: String, // Kardio Pembakar Lemak, Kekuatan Otot & Toning, Pemulihan Aktif & Fleksibilitas
    val targetCaloriesKcal: Int,
    val durationMinutes: Int,
    val intensity: String, // Ringan, Sedang, Menengah
    val description: String,
    val exercises: List<ExerciseStep>,
    val tips: String
)

data class ExerciseStep(
    val name: String,
    val repsOrDuration: String,
    val restDuration: String,
    val instruction: String
)

object WorkoutGuideData {
    val weeklySchedule = listOf(
        WorkoutGuideItem(
            dayOfWeek = "Senin",
            title = "Kardio Interval Ringan & Jalan Cepat (Brisk Walk)",
            category = "Kardio Pembakar Lemak",
            targetCaloriesKcal = 280,
            durationMinutes = 35,
            intensity = "Sedang",
            description = "Memulai awal pekan dengan pembakaran kalori stabil untuk membakar cadangan glikogen dan meningkatkan metabolisme harian.",
            exercises = listOf(
                ExerciseStep("Pemanasan Dinamis (Arm circles, Knee lifts)", "3 menit", "-", "Putar bahu dan angkat lutut perlahan untuk melumasi sendi."),
                ExerciseStep("Jalan Santai Awal", "5 menit", "-", "Kecepatan sedang untuk menaikkan denyut nadi perlahan."),
                ExerciseStep("Brisk Walk Interval (Cepat 2 mnt, Sedang 1 mnt)", "20 menit (6-7 siklus)", "-", "Jalan cepat dengan langkah mantap dan ayunan lengan teratur."),
                ExerciseStep("Pendinginan & Stretching Betis-Paha", "5 menit", "-", "Regangkan otot kaki dan tarik napas dalam.")
            ),
            tips = "Gunakan sepatu dengan bantalan empuk dan rekam aktivitasmu di Huawei Health untuk sinkronisasi otomatis!"
        ),
        WorkoutGuideItem(
            dayOfWeek = "Selasa",
            title = "Latihan Kekuatan Tubuh Bawah (Lower Body & Glutes)",
            category = "Kekuatan Otot & Toning",
            targetCaloriesKcal = 220,
            durationMinutes = 30,
            intensity = "Sedang",
            description = "Membentuk otot paha, bokong, dan pinggul yang merupakan kelompok otot terbesar pembakar kalori tubuh.",
            exercises = listOf(
                ExerciseStep("Pemanasan Sendi Panggul & Kaki", "3 menit", "-", "Regangkan pinggul dan paha depan."),
                ExerciseStep("Bodyweight Squats (atau Kursi)", "3 set x 12 repetisi", "45 detik", "Jaga dada tetap tegak, dorong pinggul ke belakang seperti hendak duduk."),
                ExerciseStep("Glute Bridges", "3 set x 15 repetisi", "45 detik", "Berbaring di matras, dorong pinggul ke atas dan kencangkan bokong di puncak gerakan."),
                ExerciseStep("Standing Calf Raises", "3 set x 20 repetisi", "30 detik", "Jinjit perlahan dan tahan 1 detik di atas."),
                ExerciseStep("Wall Sit", "3 set x 30 detik", "60 detik", "Sandarkan punggung di dinding dengan posisi paha sejajar lantai.")
            ),
            tips = "Fokus pada postur yang benar daripada kecepatan. Jaga lutut tidak melebihi ujung jari kaki."
        ),
        WorkoutGuideItem(
            dayOfWeek = "Rabu",
            title = "Pemulihan Aktif, Peregangan & Target 7000 Langkah",
            category = "Pemulihan Aktif & Fleksibilitas",
            targetCaloriesKcal = 180,
            durationMinutes = 25,
            intensity = "Ringan",
            description = "Memberi waktu regenerasi otot sambil tetap menjaga kalori harian aktif melalui jalan santai dan mobilitas tubuh.",
            exercises = listOf(
                ExerciseStep("Cat-Cow Stretch", "10 siklus napas", "-", "Lenturkan tulang belakang secara lembut."),
                ExerciseStep("Child's Pose & Hamstring Stretch", "3 menit", "-", "Rilekskan pinggang bagian bawah dan paha belakang."),
                ExerciseStep("Jalan Santai Sore Hari", "20 menit", "-", "Nikmati udara segar dan kumpulkan langkah harian di Huawei Health.")
            ),
            tips = "Pastikan asupan air putih tercapai minimal 2200 ml hari ini untuk mempercepat pengeluaran sisa metabolisme."
        ),
        WorkoutGuideItem(
            dayOfWeek = "Kamis",
            title = "Latihan Tubuh Atas & Core (Upper Body & Perut)",
            category = "Kekuatan Otot & Toning",
            targetCaloriesKcal = 210,
            durationMinutes = 30,
            intensity = "Sedang",
            description = "Memperbaiki postur, mengencangkan lengan dan perut bawah yang efektif bagi wanita 30-an.",
            exercises = listOf(
                ExerciseStep("Pemanasan Bahu & Leher", "3 menit", "-", "Regangkan bahu dan putar pergelangan tangan."),
                ExerciseStep("Wall Push-Ups / Incline Push-Ups", "3 set x 12 repetisi", "45 detik", "Dorong tubuh menggunakan kekuatan dada dan trisep."),
                ExerciseStep("Standing Dumbbell / Bottle Rows", "3 set x 15 repetisi", "45 detik", "Gunakan botol air 600ml di tiap tangan, tarik siku ke belakang untuk mengencangkan punggung."),
                ExerciseStep("Dead Bug (Perut Rata)", "3 set x 10 repetisi/sisi", "45 detik", "Berbaring, gerakkan tangan dan kaki berlawanan tanpa mengangkat punggung bawah."),
                ExerciseStep("Plank on Knees / Full Plank", "3 set x 25-30 detik", "60 detik", "Kunci otot perut dan jaga tubuh sejajar garis lurus.")
            ),
            tips = "Tarik napas saat menurunkan beban, hembuskan saat mendorong/mengencangkan core."
        ),
        WorkoutGuideItem(
            dayOfWeek = "Jumat",
            title = "Low-Impact HIIT Pembakar Lemak Kardio",
            category = "Kardio Pembakar Lemak",
            targetCaloriesKcal = 260,
            durationMinutes = 30,
            intensity = "Sedang ke Tinggi",
            description = "Kardio tanpa lompatan berlebih yang aman untuk sendi lutut, memaksimalkan afterburn effect.",
            exercises = listOf(
                ExerciseStep("Pemanasan Kardio Ringan", "3 menit", "-", "Step touch dan arm swings."),
                ExerciseStep("Step Jacks (Jumping Jacks tanpa lompat)", "40 detik kerja, 20 detik istirahat x 3 set", "20 detik", "Buka kaki bergantian ke samping sambil mengangkat tangan."),
                ExerciseStep("Standing Cross-Knee Crunch", "40 detik kerja x 3 set", "20 detik", "Sentuhkan siku kanan ke lutut kiri bergantian untuk melatih obliques perut."),
                ExerciseStep("Speed Punches & Squat Pulse", "40 detik kerja x 3 set", "20 detik", "Pukulan cepat di tempat dikombinasikan setengah squat ringan."),
                ExerciseStep("Pendinginan Menyeluruh", "5 menit", "-", "Pendinginan dan peregangan menyeluruh.")
            ),
            tips = "Minum sedikit air di sela-sela set dan jaga ritme pernapasan."
        ),
        WorkoutGuideItem(
            dayOfWeek = "Sabtu",
            title = "Aktivitas Kardio Luar Ruangan & Jalan Pagi",
            category = "Kardio Pembakar Lemak",
            targetCaloriesKcal = 320,
            durationMinutes = 45,
            intensity = "Sedang",
            description = "Manfaatkan akhir pekan dengan jalan pagi di taman atau lingkungan sekitar, menghirup udara segar dan mendapat vitamin D alami.",
            exercises = listOf(
                ExerciseStep("Jalan Santai Pembuka", "5 menit", "-", "Kecepatan santai menikmati suasana pagi."),
                ExerciseStep("Jalan Cepat Kontinu", "35 menit", "-", "Pertahankan denyut nadi zona aerobik (115-135 bpm)."),
                ExerciseStep("Peregangan Kaki & Pinggul", "5 menit", "-", "Regangkan otot kuadrisep, betis, dan punggung.")
            ),
            tips = "Screenshot hasil sesi latihan dari Huawei Health setelah selesai dan langsung unggah ke aplikasi!"
        ),
        WorkoutGuideItem(
            dayOfWeek = "Minggu",
            title = "Rest & Reset / Yoga Relaksasi",
            category = "Pemulihan Aktif & Fleksibilitas",
            targetCaloriesKcal = 120,
            durationMinutes = 20,
            intensity = "Ringan",
            description = "Hari pemulihan total untuk meredakan ketegangan otot, menyiapkan tubuh dan mental untuk pekan diet berikutnya.",
            exercises = listOf(
                ExerciseStep("Deep Diaphragmatic Breathing", "3 menit", "-", "Latihan pernapasan perut mendalam untuk menurunkan hormon kortisol."),
                ExerciseStep("Full Body Gentle Yoga Stretches", "15 menit", "-", "Gerakan peregangan lembut untuk bahu, leher, pinggang, dan panggul."),
                ExerciseStep("Evaluasi Mingguan & Timbang Badan", "2 menit", "-", "Catat timbangan mingguan dan buat laporan PDF progres mingguanmu!")
            ),
            tips = "Tidur berkualitas 7-8 jam sangat penting untuk perbaikan jaringan dan penurunan berat badan yang konsisten."
        )
    )
}
