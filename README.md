# QuoteVerse — Quotes & Shayari Social Android App ✍️✨

**QuoteVerse** is a modern, production-oriented social media Android application built with **Jetpack Compose**, **Material 3**, and **Firebase**. It provides a vibrant community platform where poets, writers, and thinkers can create, design, discover, and share quotes and shayari in Hindi, Hinglish, and English.

> [!IMPORTANT]
> **100% User-Generated Content (UGC)**: QuoteVerse is dedicated to authentic human expression. All posts, quotes, and shayari are created and curated directly by human users. There is zero AI generation or bot-driven content creation.

---

## 📱 Features Overview

### 1. 🎨 Quote Studio & Post Creation (Core Feature)
- **Designed Quote Image Studio**:
  - Live interactive Compose canvas preview.
  - 10+ artistic background presets (Warm Dusk, Golden Hour, Royal Slate, Deep Velvet, Midnight Cyber, Crimson Passion, Emerald Grove, Vintage Parchment, and custom solids/gradients).
  - Multi-line text wrapping powered by native Android `StaticLayout` with full Devanagari Hindi, Hinglish, and English rendering.
  - Font styling, font sizes (20sp–48sp), text alignment (Left, Center, Right), text colors, and author signature line.
  - **High-Resolution Export**: Renders crystal-clear 1080×1080 JPEG bitmaps with antialiasing for crisp publication.
- **Text Post Mode**:
  - Expressive typographic cards with category tagging and optional captions.
- **Image Upload Mode**:
  - Modern Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) for safe, permissionless image selection.
- **Publishing Safeguards**:
  - Validation against empty text and length limits (up to 2,000 characters).
  - Cancelable uploads, real-time progress indicators, network retry handling, and duplicate submission prevention.

### 2. 🏠 Social Home Feed
- **Triple-Tab Experience**:
  - **For You**: Discover recent quotes across the entire community.
  - **Following**: Chronological feed of creators you actively follow.
  - **Trending**: Curated feed of popular posts ranked by community engagement (`likeCount`).
- **Interactive Quote Cards**:
  - Dynamic aspect ratios, creator avatars, timestamps, and category badges.
  - Real-time like toggle with optimistic UI updates and server synchronization.
  - Comment count and full-screen comments sheet.
  - Direct post sharing via Android System Share sheet.
  - Private bookmarking/saving.
  - Overflow menu for reporting or deleting own posts.
  - AdMob native ad placeholders integrated every 5 posts without disrupting the reading flow.

### 3. 🔍 Explore & Discovery
- **Debounced Real-Time Search**: Search by username, author name, or keywords in quotes.
- **14 Bilingual Categories** (with Hindi / Devanagari titles):
  - ❤️ Love (*प्यार*)
  - 💔 Sad Shayari (*दर्द भरी शायरी*)
  - 🌿 Life Quotes (*ज़िंदगी के विचार*)
  - 🚀 Motivation (*प्रेरणा*)
  - 🔥 Attitude (*एटीट्यूड शायरी*)
  - 🤝 Friendship (*दोस्ती*)
  - 👨‍👩‍👧 Family (*परिवार*)
  - 🥀 Breakup (*ब्रेकअप शायरी*)
  - 🌹 Romantic (*रोमांटिक*)
  - 😊 Happiness (*खुशी*)
  - 🏆 Success (*सफलता*)
  - ✍️ Hindi Shayari (*हिंदी शायरी*)
  - 🌅 Good Morning (*सुप्रभात*)
  - 🎉 Festival Wishes (*त्योहार और शुभकामनाएं*)
- **Category Deep Dive**: Filtered post feeds for any category.
- **Popular Creators Spotlight**: Discover top poets and writers with one-tap follow buttons.

### 4. 💬 Social Interactions & Private Bookmarks
- **Likes**: Atomic like tracking with duplicate prevention.
- **Comments**: Chronological discussion threads; comment deletion by author; comment reporting.
- **Private Bookmarks**: Isolated subcollections (`users/{userId}/bookmarks`) protected by security rules so only the account owner can access their saved collection.

### 5. 👥 Follow & Profile System
- **Follow / Unfollow**: Mutual relationships with counter protection (prevents self-following).
- **Profile Hub**:
  - Profile avatar, username, display name, bio, follower count, following count, and post count.
  - Tabbed lists: **My Posts**, **Liked Posts**, and **Saved Bookmarks** (private to owner).
  - Edit profile with image picker and username validation.
  - User blocking and unblocking.

### 6. 🔔 Notifications & Messaging
- Push notifications via **Firebase Cloud Messaging (FCM)**.
- In-app notification center for new likes, comments, and followers.
- One-tap navigation to relevant posts or user profiles.
- Notification read tracking.

### 7. 🛡️ Content Moderation & Safety
- **Report Dialog**: Predefined report categories (Spam, Harassment, Inappropriate, Hate Speech, Copyright).
- **User Blocking**: Blocks malicious users and hides their content.
- **Admin Moderation States**: Posts feature `moderationStatus` (`APPROVED`, `PENDING`, `FLAGGED`, `REMOVED`).
- **Community Guidelines & Privacy**: Dedicated in-app safety center and guidelines.
- **Account Deletion**: Self-service account deletion workflow.

---

## 🏗️ Architecture & Technology Stack

QuoteVerse is built following modern **Android Clean Architecture** and the **MVVM (Model-View-ViewModel)** pattern:

```
com.example.quoteverse
├── QuoteVerseApp.kt                # Application class with Hilt initialization
├── MainActivity.kt                 # Edge-to-edge Compose activity
├── navigation/                     # Navigation Compose destination routes
│   ├── Screen.kt
│   └── AppNavHost.kt
├── core/
│   ├── model/                      # Immutable domain models (User, Post, Comment, Category, etc.)
│   ├── result/                     # Resource<T> sealed class for UI state
│   └── design/
│       ├── components/             # Reusable UI widgets (QuoteCard, QuoteCanvasRenderer, etc.)
├── theme/                          # Material 3 colors, typography (Devanagari support), theme
├── data/
│   ├── di/                         # Dagger Hilt dependency injection modules
│   └── repository/                 # Repository interfaces and implementations (Firebase & Mock)
└── feature/                        # Feature modules (auth, home, explore, createpost, profile, etc.)
```

### Key Libraries
- **Language**: Kotlin 2.3.20
- **UI Toolkit**: Jetpack Compose (BOM 2026.03.01) with Material 3
- **Dependency Injection**: Dagger Hilt 2.60.1 + KSP 2.3.12
- **Navigation**: Navigation Compose 2.8.8
- **Image Loading**: Coil 2.7.0
- **Authentication**: Firebase Auth + Android Credential Manager + GoogleID 1.1.1
- **Database**: Cloud Firestore (with composite indexes and security rules)
- **Cloud Storage**: Firebase Cloud Storage
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **Pagination**: AndroidX Paging 3 Compose 3.3.5

---

## 🚀 Getting Started & Firebase Setup

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 17 (`JAVA_HOME` pointing to OpenJDK 17)
- Android SDK with API 36 / Platform Tools

### Step 1: Clone the Repository
```bash
git clone <repo-url>
cd qoity
```

### Step 2: Configure Firebase
QuoteVerse includes a ready-to-test mock data mode for instant UI preview, as well as a production Firebase integration.

1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Register an Android application with package name: `com.example.quoteverse`.
3. Generate and provide your debug/release SHA-1 fingerprint for Google Sign-In:
   ```bash
   ./gradlew signingReport
   ```
4. Enable the following Firebase services:
   - **Authentication**: Enable **Email/Password** and **Google Sign-In**.
   - **Cloud Firestore**: Create database in production mode.
   - **Cloud Storage**: Create storage bucket.
   - **Cloud Messaging**: Enable FCM for push notifications.
5. Download `google-services.json` from your Firebase project settings and place it in the `app/` folder:
   ```bash
   cp ~/Downloads/google-services.json app/google-services.json
   ```
   *(A sample template is provided at `app/google-services.json.example`)*

### Step 3: Deploy Security Rules & Indexes
Deploy the included Firestore security rules, Storage rules, and composite indexes to your Firebase project:

```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login

# Associate with your project
firebase use --add

# Deploy rules and indexes
firebase deploy --only firestore:rules,storage,firestore:indexes
```

### Step 4: Toggle Mock Mode vs. Live Firebase
In `app/src/main/java/com/example/quoteverse/data/di/AppModule.kt`:

```kotlin
// Set to false for live Firebase backend, true for offline preview mock data
const val USE_MOCK_DATA = false
```

- When `USE_MOCK_DATA = false`: The app communicates with live Cloud Firestore, Firebase Storage, and Firebase Auth.
- When `USE_MOCK_DATA = true`: The app uses an in-memory `MockDataRepository` populated with authentic Hindi, Hinglish, and English quotes for instant offline demonstration.

---

## 🔒 Security Rules Architecture

QuoteVerse implements strict, production-grade security rules authored according to enterprise standards:

### Firestore Security (`firestore.rules`)
- **Anti-Impersonation**: Enforces `request.auth.uid == authorId` on post creation and profile updates.
- **Immutable Fields**: Post author IDs, post IDs, and timestamps cannot be altered after creation.
- **Private Bookmarks**: Bookmarks are stored under `users/{userId}/bookmarks/{postId}` where `request.auth.uid == userId` is strictly enforced. No other user can read or query another user's bookmarks.
- **Self-Follow Prevention**: Follow creation validates `followerId != followingId`.
- **Atomic Counters**: Clients cannot write arbitrary counter values. Counters are managed through atomic transactions or Cloud Functions.
- **Input Validation**: Text length caps (quotes $\le 2000$ chars, captions $\le 500$ chars, bios $\le 300$ chars).
- **Moderation Enforcement**: Public feeds only serve posts where `moderationStatus == 'APPROVED'`.

### Storage Security (`storage.rules`)
- Validates user authentication for all uploads.
- Restricts uploads strictly to valid image types (`image/jpeg`, `image/png`, `image/webp`).
- Size limits: $\le 5\text{MB}$ for quote images, $\le 2\text{MB}$ for profile avatars.
- File path isolation: Users can only upload to their designated storage folders (`posts/{userId}/...` and `avatars/{userId}/...`).

---

## 🧪 Testing

### Running Unit Tests
QuoteVerse includes comprehensive unit tests verifying authentication, post creation, social interactions, follow logic, moderation, and search:

```bash
./gradlew testDebugUnitTest
```

Unit test suites:
- `AuthValidationTest`: Email, password, and username uniqueness format validations.
- `PostCreationAndOwnershipTest`: Post length rules, ownership immutability, and edit boundaries.
- `SocialInteractionTest`: Likes, unliking, and private bookmark isolation.
- `FollowAndModerationTest`: Self-follow prevention, user blocking, and reporting validation.
- `SearchAndCategoryTest`: Search query filtering and 14-category bilingual mapping.

### Running UI Tests
```bash
./gradlew connectedAndroidTest
```
- `QuoteCardUiTest`: Verifies Jetpack Compose UI rendering for quote text, creator details, and interaction buttons.

---

## 📦 Building the APK

To build the debug APK:
```bash
./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
This project is open-source and intended for community poetry, quote, and shayari sharing.
All content published within the app is owned by its respective creators.
