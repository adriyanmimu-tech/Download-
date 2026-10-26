# PulseSave — Social Video Downloader & TikTok FYP Safe Optimizer

**PulseSave** হলো একটি প্রিমিয়াম **Glassmorphism UI**-ভিত্তিক সোশ্যাল মিডিয়া ভিডিও ডাউনলোডার এবং **Anti-Copyright / TikTok FYP Optimizer** অ্যাপ। এটি দিয়ে যেকোনো সোশ্যাল মিডিয়ার ভিডিও লিংক পেস্ট করে পছন্দের কোয়ালিটিতে (টাইটেল ও ক্যাপশনসহ) গ্যালারিতে ডাউনলোড করা যাবে এবং ডাউনলোডের সময় বা গ্যালারি থেকে ভিডিও ইমপোর্ট করে মেটাডাটা, ফাইল হ্যাশ ও ভিজ্যুয়াল/অডিও ফিঙ্গারপ্রিন্ট ক্লিন করা যাবে যাতে রি-আপলোড করলে **TikTok "Not Eligible for For You"** বা কপিরাইট সমস্যা না আসে।

---

## User Review & Critical Decisions

> [!IMPORTANT]
> নিচের সিদ্ধান্তগুলো আপনার উত্তরের ভিত্তিতে চূড়ান্ত করা হয়েছে। প্ল্যানটি রিভিউ করে **Proceed** বাটনে ক্লিক করলে আমরা কোডিং শুরু করব।

- **Confirmed Decision 1 (TikTok FYP & Anti-Copyright Engine)**: শুধু সাধারণ মেটাডাটা ডিলিট নয়, বরং **Deep MP4 Container Remuxing (`MediaExtractor` + `MediaMuxer`) + Binary Hash Randomizer + Micro-Frame/Track Fingerprint Modifier** যুক্ত করা হবে যাতে ভিডিওর MD5/SHA-256 হ্যাশ এবং ইন্টারনাল স্ট্রিম সিগনেচার সম্পূর্ণ নতুন হয়ে যায়।
- **Confirmed Decision 2 (Video Extraction Strategy)**: বিল্ট-ইন মাল্টি-সোর্স ওপেন ভিডিও এক্সট্র্যাক্টর (Open Extractor API + OpenGraph/Direct Stream Parser) থাকবে এবং সাথে ইউজারের সুবিধার্থে **Custom Extractor API URL / Key** যুক্ত করার অপশনও থাকবে।
- **Confirmed Decision 3 (Extra Features)**:
  1. পপ-আপ মেনু ও হিস্ট্রি থেকে **ওয়ান-ক্লিক টাইটেল, ক্যাপশন ও হ্যাশট্যাগ কপি** করার সুবিধা।
  2. **ডাউনলোড হিস্ট্রি এবং ইন-অ্যাপ ভিডিও প্লেয়ার প্রিভিউ**।
  3. **গ্যালারি থেকে যেকোনো পুরনো ভিডিও ইমপোর্ট (Local Video Scrubber)** করে মেটাডাটা ক্লিন ও FYP অপটিমাইজ করে গ্যালারিতে সেভ করার সুবিধা।

---

## 1. Overview & Core Concept

- **What It Does**:
  1. **Floating Social Hub & Link Downloader**: প্রথম পেইজে ব্যাকগ্রাউন্ড ও হিরো সেকশনে সব জনপ্রিয় সোশ্যাল মিডিয়ার আইকনগুলো (TikTok, Instagram, Facebook, X, Pinterest, Threads, Vimeo ইত্যাদি) স্মুথ অ্যানিমেশনে ভাসতে (move/float) থাকবে। মাঝখানে থাকবে গ্লাসমরফিক লিংক পেস্ট বক্স এবং ডাউনলোড বাটন।
  2. **Quality & Caption Popup Sheet**: ডাউনলোড বাটনে ক্লিক করলে একটি গ্লাসমরফিক পপ-আপ মেনু আসবে যেখানে ভিডিওর থাম্বনেইল, টাইটেল, ক্যাপশন ও হ্যাশট্যাগ (কপি বাটনসহ), ভিডিও কোয়ালিটি অপশন (`1080p Full HD`, `720p HD`, `480p SD`, `Audio MP3/M4A`) এবং **"FYP Safe Anti-Copyright Mode"** টগল থাকবে।
  3. **TikTok FYP Safe & Metadata Scrubber**: ভিডিও গ্যালারিতে সেভ হওয়ার আগে এর সব লুকানো মেটাডাটা (`moov/udta/meta` ট্যাগ, সোর্স প্ল্যাটফর্মের ওয়াটারমার্ক মেটাডাটা, এনকোডার আইডি, জিপিএস, পুরনো টাইমস্ট্যাম্প) মুছে ফেলা হবে, মাইক্রো-ফ্রেম ট্রিম ও নতুন অডিও/ভিডিও ট্র্যাক হেডার তৈরি করা হবে এবং সম্পূর্ণ নতুন MD5/SHA-256 হ্যাশ দিয়ে ফোনের গ্যালারিতে (`Movies/PulseSave`) সেভ করা হবে।
- **Target Audience / Persona**: কনটেন্ট ক্রিয়েটর, সোশ্যাল মিডিয়া ম্যানেজার এবং সাধারণ ইউজার যারা দ্রুত সোশ্যাল মিডিয়া ভিডিও সেভ করতে চান এবং কপিরাইট বা "Not Eligible for For You" স্ট্রাইক ছাড়াই ভিডিও রি-আপলোড করতে চান।
- **Key Value**: একই অ্যাপের ভেতর দ্রুত ভিডিও ডাউনলোড + ক্যাপশন কপি + প্রফেশনাল অ্যান্টি-কপিরাইট মেটাডাটা ও ফিঙ্গারপ্রিন্ট ক্লিনার।

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Home (Downloader) Flow**:
   - অ্যাপ ওপেন করলেই ইউজার দেখতে পাবেন গ্লাসমরফিক কার্ডের চারপাশে সোশ্যাল মিডিয়ার রঙিন আইকনগুলো স্মুথলি ফ্লোট/মুভ করছে।
   - ইউজার ক্লিপবোর্ড থেকে লিংক পেস্ট করে **Download** বাটনে ট্যাপ করবেন।
   - সাথে সাথে **Quality & Metadata Selection Popup** ওপেন হবে:
     - ভিডিও প্রিভিউ থাম্বনেইল ও ডিউরেশন।
     - ভিডিওর **Title, Caption & Hashtags** বক্স এবং **Copy Caption** বাটন।
     - কোয়ালিটি সিলেকশন (`1080p HD No-Watermark`, `720p HD`, `480p Fast`, `Audio Only`)।
     - **FYP Safe Anti-Copyright Shield** (Strip Metadata + Unique MD5 Hash + Micro-Frame Trim + Speed/Pitch Jitter) অন/অফ ও কাস্টমাইজ অপশন।
   - **Save to Gallery** ট্যাপ করলে রিয়েল-টাইম প্রোগ্রেস বার এবং মেটাডাটা ক্লিনিং স্ট্যাটাস দেখিয়ে সরাসরি ফোনের গ্যালারিতে ভিডিও সেভ হবে।
2. **FYP Scrubber (Gallery Video Cleaner) Flow**:
   - ইউজার ফোনের গ্যালারি থেকে যেকোনো ডাউনলোড করা বা পুরনো ভিডিও সিলেক্ট করবেন।
   - অ্যাপ ভিডিওটির বর্তমান **Original MD5 Hash, Container Tags, Encoder Info, এবং FYP Risk Score** দেখাবে।
   - **Clean & Make FYP Safe** বাটনে ক্লিক করলে ভিডিওটি রিমাক্স ও স্ক্রাব হয়ে নতুন হ্যাশসহ গ্যালারিতে সেভ হবে এবং "Before vs After" রিপোর্ট দেখাবে।
3. **Vault / History & In-App Player Flow**:
   - ডাউনলোড ও ক্লিন করা সব ভিডিওর তালিকা, ক্যাপশন কপি করার বাটন, **Before/After Hash Badge**, গ্যালারি শেয়ার এবং **In-App Video Player Modal** দিয়ে সাথে সাথে ভিডিও প্লে করে দেখার সুবিধা।

### Visual Identity & Theme
- **Aesthetic Direction**: **Cyber-Luxe Frosted Glassmorphism** — গভীর ডার্ক অরোরা ব্যাকগ্রাউন্ডের ওপর স্বচ্ছ কাঁচের মতো (Frosted Glass) লেয়ার, উজ্জ্বল নিওন বর্ডার হাইলাইট এবং ফ্লুইড অ্যানিমেশন।
- **Color Palette & Mood**:
  - `--background`: Deep Obsidian (`#070A12`) থেকে Midnight Indigo (`#0F172A`) ডায়নামিক মেশ গ্র্যাডিয়েন্ট।
  - `--surface-glass`: Translucent White/Cyan (`rgba(255, 255, 255, 0.08)`) সাথে `1.dp` লুমিনাস গ্লাস বর্ডার (`rgba(255, 255, 255, 0.18)`)।
  - `--primary`: Electric Cyan (`#00F0FF`) — দ্রুত অ্যাকশন ও ডাউনলোড হাইলাইট।
  - `--secondary`: Vivid Violet (`#8B5CF6`) — FYP Safe Optimizer ও প্রিমিয়াম ব্যাজ।
  - `--accent-success`: Emerald Glow (`#10B981`) — গ্যালারি সেভ ও "FYP Safe Verified" স্ট্যাটাস।
- **Typography & Hierarchy**:
  - হেডার ও টাইটেলের জন্য **Space Grotesk** (বোল্ড ও আধুনিক টেক লুক) এবং বডি ও ক্যাপশনের জন্য **Plus Jakarta Sans** ও হ্যাশ কোডের জন্য **JetBrains Mono** (লোকাল `.ttf` ফন্ট হিসেবে বান্ডেল করা)।
- **Interactive Feedback & Motion**:
  - `Canvas` ও `infiniteRepeatable` অ্যানিমেশনের মাধ্যমে সোশ্যাল মিডিয়া আইকনগুলোর 3D-স্টাইল ফ্লোটিং ও অরবিটিং মোশন।
  - পপ-আপ মেনু ও কার্ডের জন্য স্প্রিং-বেসড (`spring()`) স্লাইড ও ফেড অ্যানিমেশন।

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: কীভাবে TikTok "Not Eligible for For You" ও কপিরাইট সমস্যা সমাধান করা হবে?**
  - *Chosen Approach*: **Two-Stage Native Android Media Scrubber & Fingerprint Remuxer**।
    1. **Stage 1 (Native Container Remux & Micro-Trim)**: Android `MediaExtractor` এবং `MediaMuxer` ব্যবহার করে অডিও ও ভিডিও ট্র্যাক আলাদা করে পুনরায় নতুন MP4 কন্টেইনারে রিমাক্স করা হবে। এতে TikTok/Instagram/CapCut-এর লুকানো `udta`/`meta`/`XMP` কমেন্ট ও প্ল্যাটফর্ম সিগনেচার ১০০% মুছে যায়। পাশাপাশি ভিডিওর শুরু/শেষ থেকে কয়েক মিলিসেকেন্ড মাইক্রো-ট্রিম ও রোটেশন/টাইমস্কেল মেটাডাটা রি-রাইট করা হবে।
    2. **Stage 2 (Cryptographic Atom & Hash Randomizer)**: নতুন ফাইলটিতে ইউনিক `free`/`uuid` বাইনারি অ্যাটম ও নতুন ক্রিয়েশন টাইমস্ট্যাম্প যুক্ত করা হবে যাতে প্রতিবার ডাউনলোড বা ক্লিন করার পর ফাইলের **MD5 এবং SHA-256 হ্যাশ ১০০% আলাদা** হয়।
  - *Why*: ভারী ক্লাউড রেন্ডারিং ছাড়াই ফোনের ভেতরেই মাত্র ১-২ সেকেন্ডে ভিডিওর কোয়ালিটি অক্ষুণ্ণ রেখে সম্পূর্ণ নতুন ডিজিটাল ফিঙ্গারপ্রিন্ট তৈরি করা সম্ভব হয়।
- **Decision 2: সোশ্যাল মিডিয়া লিংক এক্সট্র্যাকশন ও ফলব্যাক সিস্টেম**
  - *Chosen Approach*: মাল্টি-লেয়ার এক্সট্র্যাক্টর ইঞ্জিন — যা ওপেন এক্সট্র্যাক্টর API, পেজ মেটাডাটা/OpenGraph স্ট্রিম পার্সার, ডিরেক্ট MP4 লিংক এবং ইউজারের দেওয়া কাস্টম API এন্ডপয়েন্ট সাপোর্ট করবে। এছাড়াও যেকোনো সোশ্যাল প্ল্যাটফর্মের লিংক সাথে সাথে টেস্ট করার জন্য ইনস্ট্যান্ট প্রি-কনফিগারড স্যাম্পল স্ট্রিম ও লোকাল ভিডিও স্ক্রাবার থাকবে।
  - *Why*: সোশ্যাল মিডিয়া প্ল্যাটফর্মগুলো প্রায়ই তাদের এপিআই পরিবর্তন করে, তাই মাল্টি-লেয়ার এক্সট্র্যাক্টর ও কাস্টম এপিআই অপশন থাকলে অ্যাপটি সবসময় সচল থাকবে।
- **Decision 3: গ্যালারি সেভ ও লোকাল ডাটাবেস**
  - *Chosen Approach*: গ্যালারিতে সরাসরি সেভ করার জন্য Android `MediaStore.Video.Media` (`Movies/PulseSave`) এবং ডাউনলোড হিস্ট্রি ও ক্যাপশন সংরক্ষণের জন্য লোকাল **Room Database** ব্যবহার করা হবে।
  - *Why*: Android 10+ থেকে শুরু করে আধুনিক সব ফোনে কোনো বাড়তি স্টোরেজ পারমিশন ছাড়াই সরাসরি গ্যালারিতে ভিডিও সেভ হবে এবং অফলাইনেও সব হিস্ট্রি পাওয়া যাবে।

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                           │
│  ┌─────────────────────┐  ┌──────────────────────┐  ┌──────────────────┐  │
│  │  DownloaderScreen   │  │  FypScrubberScreen   │  │  VaultHistory    │  │
│  │ • Floating Icons    │  │ • PhotoPicker Import │  │ • Saved Videos   │  │
│  │ • Glass Link Input  │  │ • Metadata Inspector │  │ • Hash Compare   │  │
│  │ • Quality Popup     │  │ • 1-Tap FYP Cleaner  │  │ • In-App Player  │  │
│  └──────────┬──────────┘  └──────────┬───────────┘  └────────┬─────────┘  │
└─────────────┼────────────────────────┼───────────────────────┼────────────┘
              │                        │                       │
              ▼                        ▼                       ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                     PulseSaveViewModel (StateFlow)                        │
│  • URL Analysis & Platform Detection   • Active Download & Scrub Progress │
│  • Quality & FYP Preset Selection      • Clipboard & Caption Copy Manager │
└─────────────┬────────────────────────┬───────────────────────┬────────────┘
              │                        │                       │
              ▼                        ▼                       ▼
┌─────────────────────────┐ ┌────────────────────────┐ ┌────────────────────┐
│  VideoExtractorService  │ │ FypMediaScrubberEngine │ │  VaultRepository   │
│ • Open API + OG Parser  │ │ • MediaExtractor/Muxer │ │ • Room Database    │
│ • Custom API Endpoint   │ │ • MP4 Box Sanitizer    │ │ • MediaStore Saver │
│ • OkHttp Streamer       │ │ • MD5/SHA256 Generator │ │ • DataStore Prefs  │
└─────────────────────────┘ └────────────────────────┘ └────────────────────┘
```

### Data Model & State
- **`VideoAnalysisResult`**:
  - `sourceUrl`, `platform` (TikTok, Instagram, Facebook, X, YouTube Shorts, Pinterest, Other), `title`, `caption`, `hashtags`, `durationSec`, `authorName`, `thumbnailUrl`, `qualities: List<VideoQualityOption>`.
- **`VideoQualityOption`**:
  - `id`, `label` (e.g., `1080p Full HD`, `720p HD`, `480p SD`, `Audio MP3`), `resolution`, `estimatedSizeMb`, `streamUrl`, `isNoWatermark`.
- **`FypOptimizationConfig`**:
  - `stripAllMetadata: Boolean`, `randomizeFileHash: Boolean`, `microTrimFrames: Boolean`, `trimMillis: Long`, `injectFreshTimestamp: Boolean`, `rewriteTrackHeaders: Boolean`.
- **`VaultVideoEntity` (Room Entity)**:
  - `id`, `title`, `caption`, `hashtags`, `platform`, `qualityLabel`, `localMediaUri`, `filePath`, `originalMd5`, `cleanedMd5`, `isFypSafe`, `removedTagsCount`, `fileSizeBytes`, `createdAt`.

### Interactive Component & State Mapping
1. **Floating Social Icons Background (`FloatingSocialOrbitCanvas`)**:
   - কাস্টম ভেক্টর ব্যাজ ও আইকন (TikTok, Instagram, Facebook, X, YouTube, Snapchat, Pinterest, Threads) যা সাইন-ওয়েভ এবং প্যারালাক্স ক্যালকুলেশনের মাধ্যমে স্ক্রিনে অবিরাম ফ্লোট করবে। যেকোনো আইকনে ট্যাপ করলে সেই প্ল্যাটফর্মের কুইক টিপ বা ডেমো লিংক টেস্ট করা যাবে।
2. **Link Paste & Quality Selection Popup (`QualitySelectorBottomSheet`)**:
   - লিংক পেস্ট করে ডাউনলোড বাটনে ক্লিক করলে লোডিং অ্যানিমেশন শেষে পপ-আপ ওপেন হবে।
   - পপ-আপের ভেতরে ইউজার টাইটেল ও ক্যাপশন এডিট বা ওয়ান-ক্লিকে কপি করতে পারবেন, রেজোলিউশন বেছে নিতে পারবেন এবং **FYP Safe Shield** অপশনগুলো কাস্টমাইজ করে **Download & Save to Gallery** বাটনে ট্যাপ করতে পারবেন।
3. **Real Binary & Container Scrubber (`FypMediaScrubberEngine`)**:
   - ডাউনলোড হওয়া ভিডিও বা গ্যালারি থেকে নেওয়া ভিডিওকে `MediaExtractor` ও `MediaMuxer` দিয়ে রিমাক্স করে সব পুরনো মেটাডাটা বক্স বাদ দেবে, মাইক্রো-ফ্রেম ট্রিম করবে, নতুন ইউনিক অ্যাটম যুক্ত করবে এবং পুরনো ও নতুন **MD5 হ্যাশ** ক্যালকুলেট করে ইউজারকে প্রমাণসহ দেখাবে।
4. **In-App Video Player & Gallery Export**:
   - সেভ হওয়ার সাথে সাথে `MediaStore.Video.Media`-এর মাধ্যমে ফোনের গ্যালারিতে (`Movies/PulseSave` অ্যালবামে) ভিডিও যুক্ত হবে এবং অ্যাপের ভেতরেই বিল্ট-ইন ভিডিও প্লেয়ারে প্লে করে দেখা যাবে।
