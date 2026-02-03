package com.example.quiz_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz_app.gallery.GalleryActivity
import com.example.quiz_app.quiz.QuizActivity

/**
 * MainActivity extends AppCompatActivity and sets up the main layout when the activity is created.
 * This one uses the XML layout as requested per the task, Gallery and Quiz use Compose
 * As such we do not use any custom buttons or backgrounds, just regular styling from XML
 *
 * I have also found this docs page:
 * https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose
 * but I don't think that's what is wanted from us, as this way we only include the XML layout to
 * a Compose file, which is not really stated in the requirements of the task
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val galleryButton: Button = findViewById(R.id.gallery_button)
        galleryButton.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        val quizButton: Button = findViewById(R.id.quiz_button)
        quizButton.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }
    }
}
