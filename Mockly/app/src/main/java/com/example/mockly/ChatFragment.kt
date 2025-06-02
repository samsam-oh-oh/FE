package com.example.mockly

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mockly.adapter.ChatAdapter
import com.example.mockly.api.InterviewApiService
import com.example.mockly.databinding.FragmentChatBinding
import com.example.mockly.model.ChatMessage
import com.example.mockly.model.FeedbackResponse
import com.example.mockly.model.QuestionResponse
import com.example.mockly.model.ScoreResponse
import com.example.mockly.util.HuggingFaceSTT
import com.example.mockly.util.WavRecorder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    private var questions: List<String> = emptyList()
    private var currentQuestionIndex = 0
    private var answers: MutableList<String> = mutableListOf()

    private var wavRecorder: WavRecorder? = null
    private var isRecording = false

    private val PICK_PDF_REQUEST = 1002
    private var selectedPdfFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        checkAudioPermission()

        chatAdapter = ChatAdapter(messageList) {
            toggleRecording()
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter

        messageList.add(ChatMessage(
            "📄 먼저 이력서 PDF를 첨부해주세요.\n첨부된 이력서를 기반으로 면접 질문을 생성합니다.",
            isUser = false
        ))
        chatAdapter.notifyDataSetChanged()

        binding.uploadPdfButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PICK_PDF_REQUEST)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                Log.d("InterviewAPI", "📄 PDF URI 선택됨: $uri")
                val file = createTempFileFromUri(uri)
                selectedPdfFile = file
                Log.d("InterviewAPI", "📄 PDF 임시 파일 경로: ${file.absolutePath}")
                requestInterviewQuestions(file)
            } ?: run {
                Log.e("InterviewAPI", "❌ PDF URI가 null입니다")
            }
        }
    }

    private fun requestInterviewQuestions(pdfFile: File) {
        Log.d("InterviewAPI", "🚀 서버에 질문 생성 요청 시작")

        val retrofit = Retrofit.Builder()
            .baseUrl("http://13.209.230.38/")
            .client(OkHttpClient.Builder().connectTimeout(180, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InterviewApiService::class.java)
        val requestFile = pdfFile.asRequestBody("application/pdf".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", pdfFile.name, requestFile)

        api.generateQuestions(body).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("InterviewAPI", "✅ 질문 생성 요청 응답: ${response.code()}")
                if (response.isSuccessful) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchQuestionsFromServer(api)
                    }, 1000)
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "❌ 질문 생성 실패\n${response.code()}: $errorMsg", Toast.LENGTH_LONG).show()
                    Log.e("InterviewAPI", "❌ 응답 실패 내용: $errorMsg")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "❌ 네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                Log.e("InterviewAPI", "❌ 요청 실패: ${t.message}", t)
            }
        })
    }

    private fun fetchQuestionsFromServer(api: InterviewApiService) {
        api.getQuestions().enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(call: Call<QuestionResponse>, response: Response<QuestionResponse>) {
                Log.d("InterviewAPI", "✅ 질문 가져오기 응답: ${response.code()}")
                Log.d("InterviewAPI", "📦 전체 응답 JSON: ${response.body()}")

                val questionList = response.body()?.data?.questionList
                Log.d("InterviewAPI", "📋 받아온 questionList: $questionList")

                if (!questionList.isNullOrEmpty()) {
                    questions = questionList
                    currentQuestionIndex = 0
                    Toast.makeText(requireContext(), "✅ 질문 수신 완료", Toast.LENGTH_SHORT).show()
                    showCurrentQuestion()
                } else {
                    Toast.makeText(requireContext(), "❗ 질문이 비어 있습니다.", Toast.LENGTH_LONG).show()
                    Log.e("InterviewAPI", "❌ 응답은 성공했지만 질문이 비어 있음")
                }
            }

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "❌ 질문 가져오기 실패", Toast.LENGTH_SHORT).show()
                Log.e("InterviewAPI", "❌ 질문 GET 요청 실패: ${t.message}", t)
            }
        })
    }

    private fun toggleRecording() {
        if (!isRecording) {
            val filename = "response_${System.currentTimeMillis()}.wav"
            wavRecorder = WavRecorder(filename, requireContext())
            wavRecorder?.startRecording()
            isRecording = true
            Toast.makeText(requireContext(), "🎤 녹음 시작", Toast.LENGTH_SHORT).show()
        } else {
            wavRecorder?.stopRecording()
            val wavFile = wavRecorder?.getWavFile()
            isRecording = false
            messageList.removeAll { it.isRecordingPrompt }

            if (wavFile != null && wavFile.exists()) {
                HuggingFaceSTT.sendAudioToHF(wavFile) { resultText ->
                    activity?.runOnUiThread {
                        answers.add(resultText)
                        messageList.add(ChatMessage(resultText, isUser = true))
                        currentQuestionIndex++
                        showCurrentQuestion()

                        if (answers.size == questions.size) {
                            val answerText = answers.joinToString(separator = "\n\n")
                            val fileName = "interview_answers_${System.currentTimeMillis()}.txt"
                            val file = File(requireContext().getExternalFilesDir(null), fileName)
                            file.writeText(answerText)

                            Log.d("InterviewAPI", "📤 서버로 전송할 텍스트 파일 경로: ${file.absolutePath}")
                            uploadAnswerTextFile(file)
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "❌ WAV 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                currentQuestionIndex++
                showCurrentQuestion()
            }
        }
    }

    private fun showCurrentQuestion() {
        Log.d("InterviewAPI", "🎯 현재 질문 인덱스: $currentQuestionIndex")

        val displayIndex = currentQuestionIndex
        val currentQuestion = questions.getOrNull(displayIndex)

        Log.d("InterviewAPI", "📝 현재 질문 내용: $currentQuestion")

        if (currentQuestion != null) {
            messageList.add(ChatMessage(currentQuestion.trim(), isUser = false))
            messageList.add(ChatMessage("", isUser = false, isRecordingPrompt = true))

            chatAdapter.notifyDataSetChanged()
            binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
        } else {
            messageList.add(
                ChatMessage(
                    "✅ 고생하셨습니다. 면접이 종료되었습니다. 결과 리포트를 기다려 주세요.",
                    isUser = false
                )
            )
            chatAdapter.notifyDataSetChanged()
            binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun uploadAnswerTextFile(file: File) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://13.209.230.38/")
            .client(OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InterviewApiService::class.java)
        val requestFile = file.asRequestBody("text/plain".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("STT_file", file.name, requestFile)

        api.uploadAnswerText(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "✅ 답변 텍스트 전송 완료", Toast.LENGTH_SHORT).show()
                    Log.d("InterviewAPI", "✅ 서버 응답: ${response.body()?.string()}")
                    fetchFeedbackFromServer() // ✅ 전송 완료 후 피드백 호출
                } else {
                    Log.e("InterviewAPI", "❌ 응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("InterviewAPI", "❌ 전송 실패: ${t.localizedMessage}", t)
            }
        })
    }

    private fun fetchFeedbackFromServer() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://13.209.230.38/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InterviewApiService::class.java)

        api.getFeedbacks().enqueue(object : Callback<FeedbackResponse> {
            override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                if (response.isSuccessful) {
                    val feedbacks = response.body()?.feedbackList.orEmpty()
                    feedbacks.forEach { feedback ->
                        messageList.add(ChatMessage("🧠 피드백: $feedback", isUser = false))
                    }
                    chatAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                    Log.d("InterviewAPI", "✅ 피드백 수신 완료: $feedbacks")

                    // ✅ 여기서 점수까지 이어서 받기
                    fetchScoresFromServer()
                } else {
                    Log.e("InterviewAPI", "❌ 피드백 응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                Log.e("InterviewAPI", "❌ 피드백 요청 실패: ${t.message}", t)
            }
        })
    }
    private fun fetchScoresFromServer() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://13.209.230.38/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(InterviewApiService::class.java)

        api.getScores().enqueue(object : Callback<ScoreResponse> {
            override fun onResponse(call: Call<ScoreResponse>, response: Response<ScoreResponse>) {
                if (response.isSuccessful) {
                    val scoreMap = response.body()?.scoreMap.orEmpty()
                    val scoreText = scoreMap.entries.joinToString("\n") { "${it.key}: ${it.value}점" }

                    messageList.add(
                        ChatMessage("📊 면접 점수 결과입니다:\n$scoreText", isUser = false)
                    )
                    chatAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                    Log.d("InterviewAPI", "✅ 점수 수신 완료: $scoreText")
                } else {
                    Log.e("InterviewAPI", "❌ 점수 응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                Log.e("InterviewAPI", "❌ 점수 요청 실패: ${t.message}", t)
            }
        })
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                1001
            )
        }
    }

    private fun createTempFileFromUri(uri: Uri): File {
        Log.d("InterviewAPI", "📄 PDF URI에서 파일 생성 중")
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val fileName = "resume_${System.currentTimeMillis()}.pdf"
        val tempFile = File(requireContext().cacheDir, fileName)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "🎤 마이크 권한 허용됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "❌ 마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}