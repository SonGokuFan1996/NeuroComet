const fs = require('fs');
const path = require('path');

const resDir = path.join(__dirname, '..', 'app', 'src', 'main', 'res');

// All translations for missing keys per language
const translations = {
  tr: {
    // Debug overlay
    debug_fps: 'FPS: %d',
    debug_jank: '⚠️ TAKINTI',
    debug_drops: 'Düşmeler: %d',
    debug_mem: 'Bellek: %.1f MB',
    debug_scroll: 'Kaydırma: %d px/s',
    // Account management
    account_delete: 'Hesabı Sil',
    account_delete_confirm: 'Hesabınızı silmek istediğinizden emin misiniz?',
    account_delete_warning: 'Bu işlem geri alınamaz. Tüm verileriniz kalıcı olarak silinecektir.',
    account_delete_scheduled: 'Hesap 14 gün içinde silinmek üzere planlandı. İptal etmek için tekrar giriş yapın.',
    account_delete_cancel: 'Silmeyi İptal Et',
    account_delete_cancelled: 'Hesap silme iptal edildi',
    // Email
    email_verification: 'E-posta Doğrulama',
    email_resend_verification: 'Doğrulama E-postasını Tekrar Gönder',
    email_verification_sent: 'Doğrulama e-postası gönderildi!',
    email_not_verified: 'E-postanız doğrulanmamış',
    email_verify: 'E-postayı Doğrula',
    // Password
    password_update: 'Şifreyi Güncelle',
    password_updated: 'Şifre başarıyla güncellendi',
    email_update: 'E-postayı Güncelle',
    password_current: 'Mevcut Şifre',
    password_new: 'Yeni Şifre',
    // User actions
    user_blocked: 'Kullanıcı engellendi',
    user_unblocked: 'Kullanıcının engeli kaldırıldı',
    user_muted: 'Kullanıcı susturuldu',
    user_unmuted: 'Kullanıcının sesi açıldı',
    // Report
    report_submitted: 'Rapor gönderildi. En kısa sürede inceleyeceğiz.',
    report_reason: 'Raporlama nedeni',
    report_details: 'Ek ayrıntılar (isteğe bağlı)',
    report_spam: 'Spam',
    report_harassment: 'Taciz',
    report_hate_speech: 'Nefret söylemi',
    report_misinformation: 'Yanlış bilgi',
    report_violence: 'Şiddet',
    report_other: 'Diğer',
    // Actions
    action_unblock: 'Engeli Kaldır',
    action_unmute: 'Sesi Aç',
    blocked_users_empty: 'Engellenen kullanıcı yok',
    muted_users_empty: 'Sessize alınan kullanıcı yok',
    // Bookmarks
    bookmark: 'Yer İmi',
    bookmark_added: 'Gönderi yer imlerine eklendi',
    bookmark_removed: 'Yer imi kaldırıldı',
    bookmarks: 'Yer İmleri',
    bookmarks_empty: 'Henüz yer imi yok',
    bookmarks_hint: 'Yer imlerine eklediğiniz gönderiler burada görünecek.',
    // Errors
    error_connection: 'Bağlantı hatası. Lütfen internetinizi kontrol edin.',
    error_server: 'Sunucu hatası. Lütfen daha sonra tekrar deneyin.',
    error_unknown: 'Bir şeyler ters gitti. Lütfen tekrar deneyin.',
    toast_offline: 'Çevrimdışısınız',
    toast_online: 'Tekrar çevrimiçi',
    error_session_expired: 'Oturum süresi doldu. Lütfen tekrar giriş yapın.',
    error_rate_limited: 'Çok fazla istek. Lütfen bir süre bekleyin.',
    error_no_internet: 'İnternet bağlantısı yok',
    action_try_again: 'Tekrar Dene',
    action_dismiss: 'Kapat',
    // Search
    search_users: 'Kullanıcı ara',
    search_posts: 'Gönderi ara',
    search_no_results: 'Sonuç bulunamadı',
    search_hint: 'Farklı bir arama terimi deneyin',
    // Comments
    comments_view_replies: '%d yanıtı görüntüle',
    comments_hide_replies: 'Yanıtları gizle',
    // Time
    time_seconds_ago: '%ds önce',
    time_minute_ago: '1 dakika önce',
    time_hour_ago: '1 saat önce',
    time_day_ago: '1 gün önce',
    time_week_ago: '1 hafta önce',
    time_month_ago: '1 ay önce',
    time_year_ago: '1 yıl önce',
    // Accessibility
    a11y_skip_to_content: 'İçeriğe atla',
    a11y_close_dialog: 'İletişim kutusunu kapat',
    a11y_open_menu: 'Menüyü aç',
    a11y_close_menu: 'Menüyü kapat',
    a11y_go_back: 'Geri git',
    a11y_scroll_to_top: 'Yukarı kaydır',
    a11y_load_more: 'Daha fazla yükle',
    a11y_refresh: 'İçeriği yenile',
    a11y_image_desc: 'Resim açıklaması',
    a11y_video_desc: 'Video açıklaması',
    // Conversation
    conversation_video_call: 'Görüntülü arama',
    conversation_voice_call: 'Sesli arama',
    conversation_back: 'Geri',
    conversation_profile_picture: 'Profil fotoğrafı',
    conversation_more_options: 'Daha fazla seçenek',
    conversation_scroll_to_bottom: 'Aşağı kaydır',
    conversation_remove_attachment: 'Eki kaldır',
    conversation_cancel_recording: 'Kaydı iptal et',
    conversation_take_photo: 'Fotoğraf çek',
    conversation_sending: 'Gönderiliyor',
    conversation_sent: 'Gönderildi',
    conversation_failed: 'Başarısız',
    conversation_verified: 'Doğrulandı',
    conversation_search: 'Ara',
    conversation_practice_calls: 'Pratik Aramalar',
    conversation_new_message: 'Yeni Mesaj',
    // Simulation
    sim_offline_error: 'Ağ bağlantısı yok (simüle edilmiş çevrimdışı)',
    sim_server_error: 'Simüle edilmiş sunucu hatası (HTTP 500)',
    // Permissions
    camera_permission_required: 'Kamera izni gerekli',
    location_permission_required: 'Konum izni gerekli',
    contacts_permission_required: 'Kişiler izni gerekli',
    microphone_permission_required: 'Mikrofon izni gerekli',
    // Location
    getting_location: 'Konum alınıyor…',
    location_shared: 'Konum paylaşıldı',
    unable_to_get_location: 'Konum alınamadı',
    shared_location: 'Paylaşılan Konum',
    // Chat
    new_chat: 'Yeni Sohbet',
    sync_contacts: 'Kişileri Senkronize Et',
    allow_access_find_friends: 'NeuroComet\\\'te arkadaşlarınızı bulmak için erişime izin verin',
    not_now: 'Şimdi Değil',
    allow: 'İzin Ver',
    contacts_synced: 'Kişiler senkronize edildi',
    // Auth validation
    email_required: 'E-posta gerekli',
    invalid_email: 'Lütfen geçerli bir e-posta adresi girin',
    password_required: 'Şifre gerekli',
    password_too_short: 'Şifre en az 6 karakter olmalıdır',
    auth_required: 'Kimlik doğrulama gerekli',
  },

  es: {
    // Debug overlay
    debug_fps: 'FPS: %d',
    debug_jank: '⚠️ TIRÓN',
    debug_drops: 'Caídas: %d',
    debug_mem: 'Mem: %.1f MB',
    debug_scroll: 'Despl: %d px/s',
    // Conversation
    conversation_video_call: 'Videollamada',
    conversation_voice_call: 'Llamada de voz',
    conversation_back: 'Atrás',
    conversation_profile_picture: 'Foto de perfil',
    conversation_more_options: 'Más opciones',
    conversation_scroll_to_bottom: 'Desplazar al final',
    conversation_remove_attachment: 'Eliminar adjunto',
    conversation_cancel_recording: 'Cancelar grabación',
    conversation_take_photo: 'Tomar foto',
    conversation_sending: 'Enviando',
    conversation_sent: 'Enviado',
    conversation_failed: 'Error',
    conversation_verified: 'Verificado',
    conversation_search: 'Buscar',
    conversation_practice_calls: 'Llamadas de práctica',
    conversation_new_message: 'Nuevo mensaje',
    // Simulation
    sim_offline_error: 'Sin conexión de red (simulación sin conexión)',
    sim_server_error: 'Error de servidor simulado (HTTP 500)',
    // Permissions
    camera_permission_required: 'Se requiere permiso de cámara',
    location_permission_required: 'Se requiere permiso de ubicación',
    contacts_permission_required: 'Se requiere permiso de contactos',
    microphone_permission_required: 'Se requiere permiso de micrófono',
    // Location
    getting_location: 'Obteniendo ubicación…',
    location_shared: 'Ubicación compartida',
    unable_to_get_location: 'No se pudo obtener la ubicación',
    shared_location: 'Ubicación compartida',
    // Chat
    new_chat: 'Nuevo chat',
    sync_contacts: 'Sincronizar contactos',
    allow_access_find_friends: 'Permitir acceso para encontrar amigos en NeuroComet',
    not_now: 'Ahora no',
    allow: 'Permitir',
    contacts_synced: 'Contactos sincronizados',
    // Auth validation
    email_required: 'El correo electrónico es obligatorio',
    invalid_email: 'Introduce una dirección de correo válida',
    password_required: 'La contraseña es obligatoria',
    password_too_short: 'La contraseña debe tener al menos 6 caracteres',
    auth_required: 'Se requiere autenticación',
  },

  de: {
    // Debug overlay
    debug_fps: 'FPS: %d',
    debug_jank: '⚠️ RUCKLER',
    debug_drops: 'Drops: %d',
    debug_mem: 'Speicher: %.1f MB',
    debug_scroll: 'Scrollen: %d px/s',
    // Account management
    account_delete: 'Konto löschen',
    account_delete_confirm: 'Sind Sie sicher, dass Sie Ihr Konto löschen möchten?',
    account_delete_warning: 'Diese Aktion kann nicht rückgängig gemacht werden. Alle Ihre Daten werden dauerhaft gelöscht.',
    account_delete_scheduled: 'Konto zur Löschung in 14 Tagen vorgemerkt. Melden Sie sich erneut an, um abzubrechen.',
    account_delete_cancel: 'Löschung abbrechen',
    account_delete_cancelled: 'Kontolöschung abgebrochen',
    // Email
    email_verification: 'E-Mail-Verifizierung',
    email_resend_verification: 'Bestätigungsmail erneut senden',
    email_verification_sent: 'Bestätigungsmail gesendet!',
    email_not_verified: 'Ihre E-Mail ist nicht verifiziert',
    email_verify: 'E-Mail verifizieren',
    // Password
    password_update: 'Passwort aktualisieren',
    password_updated: 'Passwort erfolgreich aktualisiert',
    email_update: 'E-Mail aktualisieren',
    password_current: 'Aktuelles Passwort',
    password_new: 'Neues Passwort',
    // User actions
    user_blocked: 'Benutzer blockiert',
    user_unblocked: 'Blockierung aufgehoben',
    user_muted: 'Benutzer stummgeschaltet',
    user_unmuted: 'Stummschaltung aufgehoben',
    // Report
    report_submitted: 'Meldung eingereicht. Wir werden sie in Kürze überprüfen.',
    report_reason: 'Grund der Meldung',
    report_details: 'Zusätzliche Details (optional)',
    report_spam: 'Spam',
    report_harassment: 'Belästigung',
    report_hate_speech: 'Hassrede',
    report_misinformation: 'Fehlinformation',
    report_violence: 'Gewalt',
    report_other: 'Sonstiges',
    // Actions
    action_unblock: 'Entblocken',
    action_unmute: 'Stummschaltung aufheben',
    blocked_users_empty: 'Keine blockierten Benutzer',
    muted_users_empty: 'Keine stummgeschalteten Benutzer',
    // Bookmarks
    bookmark: 'Lesezeichen',
    bookmark_added: 'Beitrag als Lesezeichen gespeichert',
    bookmark_removed: 'Lesezeichen entfernt',
    bookmarks: 'Lesezeichen',
    bookmarks_empty: 'Noch keine Lesezeichen',
    bookmarks_hint: 'Beiträge, die Sie als Lesezeichen speichern, erscheinen hier.',
    // Errors
    error_connection: 'Verbindungsfehler. Bitte überprüfen Sie Ihre Internetverbindung.',
    error_server: 'Serverfehler. Bitte versuchen Sie es später erneut.',
    error_unknown: 'Etwas ist schiefgelaufen. Bitte versuchen Sie es erneut.',
    toast_offline: 'Sie sind offline',
    toast_online: 'Wieder online',
    error_session_expired: 'Sitzung abgelaufen. Bitte melden Sie sich erneut an.',
    error_rate_limited: 'Zu viele Anfragen. Bitte warten Sie einen Moment.',
    error_no_internet: 'Keine Internetverbindung',
    action_try_again: 'Erneut versuchen',
    action_dismiss: 'Verwerfen',
    // Search
    search_users: 'Benutzer suchen',
    search_posts: 'Beiträge suchen',
    search_no_results: 'Keine Ergebnisse gefunden',
    search_hint: 'Versuchen Sie einen anderen Suchbegriff',
    // Comments
    comments_view_replies: '%d Antworten anzeigen',
    comments_hide_replies: 'Antworten ausblenden',
    // Time
    time_seconds_ago: 'vor %ds',
    time_minute_ago: 'vor 1 Minute',
    time_hour_ago: 'vor 1 Stunde',
    time_day_ago: 'vor 1 Tag',
    time_week_ago: 'vor 1 Woche',
    time_month_ago: 'vor 1 Monat',
    time_year_ago: 'vor 1 Jahr',
    // Accessibility
    a11y_skip_to_content: 'Zum Inhalt springen',
    a11y_close_dialog: 'Dialog schließen',
    a11y_open_menu: 'Menü öffnen',
    a11y_close_menu: 'Menü schließen',
    a11y_go_back: 'Zurück',
    a11y_scroll_to_top: 'Nach oben scrollen',
    a11y_load_more: 'Mehr laden',
    a11y_refresh: 'Inhalt aktualisieren',
    a11y_image_desc: 'Bildbeschreibung',
    a11y_video_desc: 'Videobeschreibung',
    // Conversation
    conversation_video_call: 'Videoanruf',
    conversation_voice_call: 'Sprachanruf',
    conversation_back: 'Zurück',
    conversation_profile_picture: 'Profilbild',
    conversation_more_options: 'Weitere Optionen',
    conversation_scroll_to_bottom: 'Nach unten scrollen',
    conversation_remove_attachment: 'Anhang entfernen',
    conversation_cancel_recording: 'Aufnahme abbrechen',
    conversation_take_photo: 'Foto aufnehmen',
    conversation_sending: 'Wird gesendet',
    conversation_sent: 'Gesendet',
    conversation_failed: 'Fehlgeschlagen',
    conversation_verified: 'Verifiziert',
    conversation_search: 'Suchen',
    conversation_practice_calls: 'Übungsanrufe',
    conversation_new_message: 'Neue Nachricht',
    // Simulation
    sim_offline_error: 'Keine Netzwerkverbindung (simuliert offline)',
    sim_server_error: 'Simulierter Serverfehler (HTTP 500)',
    // Permissions
    camera_permission_required: 'Kameraberechtigung erforderlich',
    location_permission_required: 'Standortberechtigung erforderlich',
    contacts_permission_required: 'Kontaktberechtigung erforderlich',
    microphone_permission_required: 'Mikrofonberechtigung erforderlich',
    // Location
    getting_location: 'Standort wird ermittelt…',
    location_shared: 'Standort geteilt',
    unable_to_get_location: 'Standort konnte nicht ermittelt werden',
    shared_location: 'Geteilter Standort',
    // Chat
    new_chat: 'Neuer Chat',
    sync_contacts: 'Kontakte synchronisieren',
    allow_access_find_friends: 'Zugriff erlauben, um Freunde auf NeuroComet zu finden',
    not_now: 'Nicht jetzt',
    allow: 'Erlauben',
    contacts_synced: 'Kontakte synchronisiert',
    // Auth validation
    email_required: 'E-Mail ist erforderlich',
    invalid_email: 'Bitte geben Sie eine gültige E-Mail-Adresse ein',
    password_required: 'Passwort ist erforderlich',
    password_too_short: 'Das Passwort muss mindestens 6 Zeichen lang sein',
    auth_required: 'Authentifizierung erforderlich',
  },

  fr: {
    // Debug overlay
    debug_fps: 'IPS : %d',
    debug_jank: '⚠️ SACCADE',
    debug_drops: 'Pertes : %d',
    debug_mem: 'Mém : %.1f Mo',
    debug_scroll: 'Défil : %d px/s',
    // Account management
    account_delete: 'Supprimer le compte',
    account_delete_confirm: 'Êtes-vous sûr de vouloir supprimer votre compte ?',
    account_delete_warning: 'Cette action est irréversible. Toutes vos données seront définitivement supprimées.',
    account_delete_scheduled: 'Compte programmé pour suppression dans 14 jours. Reconnectez-vous pour annuler.',
    account_delete_cancel: 'Annuler la suppression',
    account_delete_cancelled: 'Suppression du compte annulée',
    // Email
    email_verification: 'Vérification de l\\\'e-mail',
    email_resend_verification: 'Renvoyer l\\\'e-mail de vérification',
    email_verification_sent: 'E-mail de vérification envoyé !',
    email_not_verified: 'Votre e-mail n\\\'est pas vérifié',
    email_verify: 'Vérifier l\\\'e-mail',
    // Password
    password_update: 'Mettre à jour le mot de passe',
    password_updated: 'Mot de passe mis à jour avec succès',
    email_update: 'Mettre à jour l\\\'e-mail',
    password_current: 'Mot de passe actuel',
    password_new: 'Nouveau mot de passe',
    // User actions
    user_blocked: 'Utilisateur bloqué',
    user_unblocked: 'Utilisateur débloqué',
    user_muted: 'Utilisateur mis en sourdine',
    user_unmuted: 'Sourdine désactivée',
    // Report
    report_submitted: 'Signalement envoyé. Nous l\\\'examinerons sous peu.',
    report_reason: 'Raison du signalement',
    report_details: 'Détails supplémentaires (facultatif)',
    report_spam: 'Spam',
    report_harassment: 'Harcèlement',
    report_hate_speech: 'Discours haineux',
    report_misinformation: 'Désinformation',
    report_violence: 'Violence',
    report_other: 'Autre',
    // Actions
    action_unblock: 'Débloquer',
    action_unmute: 'Réactiver le son',
    blocked_users_empty: 'Aucun utilisateur bloqué',
    muted_users_empty: 'Aucun utilisateur en sourdine',
    // Bookmarks
    bookmark: 'Signet',
    bookmark_added: 'Publication ajoutée aux signets',
    bookmark_removed: 'Signet supprimé',
    bookmarks: 'Signets',
    bookmarks_empty: 'Aucun signet pour le moment',
    bookmarks_hint: 'Les publications que vous mettez en signet apparaîtront ici.',
    // Errors
    error_connection: 'Erreur de connexion. Veuillez vérifier votre connexion Internet.',
    error_server: 'Erreur du serveur. Veuillez réessayer plus tard.',
    error_unknown: 'Une erreur s\\\'est produite. Veuillez réessayer.',
    toast_offline: 'Vous êtes hors ligne',
    toast_online: 'De retour en ligne',
    error_session_expired: 'Session expirée. Veuillez vous reconnecter.',
    error_rate_limited: 'Trop de requêtes. Veuillez patienter un instant.',
    error_no_internet: 'Pas de connexion Internet',
    action_try_again: 'Réessayer',
    action_dismiss: 'Ignorer',
    // Search
    search_users: 'Rechercher des utilisateurs',
    search_posts: 'Rechercher des publications',
    search_no_results: 'Aucun résultat trouvé',
    search_hint: 'Essayez un autre terme de recherche',
    // Comments
    comments_view_replies: 'Voir %d réponses',
    comments_hide_replies: 'Masquer les réponses',
    // Time
    time_seconds_ago: 'il y a %ds',
    time_minute_ago: 'il y a 1 minute',
    time_hour_ago: 'il y a 1 heure',
    time_day_ago: 'il y a 1 jour',
    time_week_ago: 'il y a 1 semaine',
    time_month_ago: 'il y a 1 mois',
    time_year_ago: 'il y a 1 an',
    // Accessibility
    a11y_skip_to_content: 'Passer au contenu',
    a11y_close_dialog: 'Fermer la boîte de dialogue',
    a11y_open_menu: 'Ouvrir le menu',
    a11y_close_menu: 'Fermer le menu',
    a11y_go_back: 'Retour',
    a11y_scroll_to_top: 'Défiler vers le haut',
    a11y_load_more: 'Charger plus',
    a11y_refresh: 'Actualiser le contenu',
    a11y_image_desc: 'Description de l\\\'image',
    a11y_video_desc: 'Description de la vidéo',
    // Conversation
    conversation_video_call: 'Appel vidéo',
    conversation_voice_call: 'Appel vocal',
    conversation_back: 'Retour',
    conversation_profile_picture: 'Photo de profil',
    conversation_more_options: 'Plus d\\\'options',
    conversation_scroll_to_bottom: 'Défiler vers le bas',
    conversation_remove_attachment: 'Supprimer la pièce jointe',
    conversation_cancel_recording: 'Annuler l\\\'enregistrement',
    conversation_take_photo: 'Prendre une photo',
    conversation_sending: 'Envoi en cours',
    conversation_sent: 'Envoyé',
    conversation_failed: 'Échec',
    conversation_verified: 'Vérifié',
    conversation_search: 'Rechercher',
    conversation_practice_calls: 'Appels d\\\'entraînement',
    conversation_new_message: 'Nouveau message',
    // Simulation
    sim_offline_error: 'Pas de connexion réseau (simulation hors ligne)',
    sim_server_error: 'Erreur serveur simulée (HTTP 500)',
    // Permissions
    camera_permission_required: 'Autorisation de la caméra requise',
    location_permission_required: 'Autorisation de localisation requise',
    contacts_permission_required: 'Autorisation des contacts requise',
    microphone_permission_required: 'Autorisation du microphone requise',
    // Location
    getting_location: 'Obtention de la position…',
    location_shared: 'Position partagée',
    unable_to_get_location: 'Impossible d\\\'obtenir la position',
    shared_location: 'Position partagée',
    // Chat
    new_chat: 'Nouvelle discussion',
    sync_contacts: 'Synchroniser les contacts',
    allow_access_find_friends: 'Autoriser l\\\'accès pour trouver des amis sur NeuroComet',
    not_now: 'Pas maintenant',
    allow: 'Autoriser',
    contacts_synced: 'Contacts synchronisés',
    // Auth validation
    email_required: 'L\\\'e-mail est requis',
    invalid_email: 'Veuillez entrer une adresse e-mail valide',
    password_required: 'Le mot de passe est requis',
    password_too_short: 'Le mot de passe doit comporter au moins 6 caractères',
    auth_required: 'Authentification requise',
  },

  pt: {
    // Debug overlay
    debug_fps: 'FPS: %d',
    debug_jank: '⚠️ TRAVAMENTO',
    debug_drops: 'Quedas: %d',
    debug_mem: 'Mem: %.1f MB',
    debug_scroll: 'Rolagem: %d px/s',
    // Account management
    account_delete: 'Excluir conta',
    account_delete_confirm: 'Tem certeza de que deseja excluir sua conta?',
    account_delete_warning: 'Esta ação não pode ser desfeita. Todos os seus dados serão excluídos permanentemente.',
    account_delete_scheduled: 'Conta programada para exclusão em 14 dias. Faça login novamente para cancelar.',
    account_delete_cancel: 'Cancelar exclusão',
    account_delete_cancelled: 'Exclusão da conta cancelada',
    // Email
    email_verification: 'Verificação de e-mail',
    email_resend_verification: 'Reenviar e-mail de verificação',
    email_verification_sent: 'E-mail de verificação enviado!',
    email_not_verified: 'Seu e-mail não foi verificado',
    email_verify: 'Verificar e-mail',
    // Password
    password_update: 'Atualizar senha',
    password_updated: 'Senha atualizada com sucesso',
    email_update: 'Atualizar e-mail',
    password_current: 'Senha atual',
    password_new: 'Nova senha',
    // User actions
    user_blocked: 'Usuário bloqueado',
    user_unblocked: 'Usuário desbloqueado',
    user_muted: 'Usuário silenciado',
    user_unmuted: 'Som do usuário reativado',
    // Report
    report_submitted: 'Denúncia enviada. Analisaremos em breve.',
    report_reason: 'Motivo da denúncia',
    report_details: 'Detalhes adicionais (opcional)',
    report_spam: 'Spam',
    report_harassment: 'Assédio',
    report_hate_speech: 'Discurso de ódio',
    report_misinformation: 'Desinformação',
    report_violence: 'Violência',
    report_other: 'Outro',
    // Actions
    action_unblock: 'Desbloquear',
    action_unmute: 'Reativar som',
    blocked_users_empty: 'Nenhum usuário bloqueado',
    muted_users_empty: 'Nenhum usuário silenciado',
    // Bookmarks
    bookmark: 'Favorito',
    bookmark_added: 'Publicação adicionada aos favoritos',
    bookmark_removed: 'Favorito removido',
    bookmarks: 'Favoritos',
    bookmarks_empty: 'Nenhum favorito ainda',
    bookmarks_hint: 'Publicações que você adicionar aos favoritos aparecerão aqui.',
    // Errors
    error_connection: 'Erro de conexão. Verifique sua internet.',
    error_server: 'Erro no servidor. Tente novamente mais tarde.',
    error_unknown: 'Algo deu errado. Tente novamente.',
    toast_offline: 'Você está offline',
    toast_online: 'De volta online',
    error_session_expired: 'Sessão expirada. Faça login novamente.',
    error_rate_limited: 'Muitas solicitações. Aguarde um momento.',
    error_no_internet: 'Sem conexão com a internet',
    action_try_again: 'Tentar novamente',
    action_dismiss: 'Dispensar',
    // Search
    search_users: 'Pesquisar usuários',
    search_posts: 'Pesquisar publicações',
    search_no_results: 'Nenhum resultado encontrado',
    search_hint: 'Tente um termo de pesquisa diferente',
    // Comments
    comments_view_replies: 'Ver %d respostas',
    comments_hide_replies: 'Ocultar respostas',
    // Time
    time_seconds_ago: '%ds atrás',
    time_minute_ago: '1 minuto atrás',
    time_hour_ago: '1 hora atrás',
    time_day_ago: '1 dia atrás',
    time_week_ago: '1 semana atrás',
    time_month_ago: '1 mês atrás',
    time_year_ago: '1 ano atrás',
    // Accessibility
    a11y_skip_to_content: 'Pular para o conteúdo',
    a11y_close_dialog: 'Fechar diálogo',
    a11y_open_menu: 'Abrir menu',
    a11y_close_menu: 'Fechar menu',
    a11y_go_back: 'Voltar',
    a11y_scroll_to_top: 'Rolar para o topo',
    a11y_load_more: 'Carregar mais',
    a11y_refresh: 'Atualizar conteúdo',
    a11y_image_desc: 'Descrição da imagem',
    a11y_video_desc: 'Descrição do vídeo',
    // Conversation
    conversation_video_call: 'Chamada de vídeo',
    conversation_voice_call: 'Chamada de voz',
    conversation_back: 'Voltar',
    conversation_profile_picture: 'Foto de perfil',
    conversation_more_options: 'Mais opções',
    conversation_scroll_to_bottom: 'Rolar para baixo',
    conversation_remove_attachment: 'Remover anexo',
    conversation_cancel_recording: 'Cancelar gravação',
    conversation_take_photo: 'Tirar foto',
    conversation_sending: 'Enviando',
    conversation_sent: 'Enviado',
    conversation_failed: 'Falhou',
    conversation_verified: 'Verificado',
    conversation_search: 'Pesquisar',
    conversation_practice_calls: 'Chamadas de prática',
    conversation_new_message: 'Nova mensagem',
    // Simulation
    sim_offline_error: 'Sem conexão de rede (simulação offline)',
    sim_server_error: 'Erro de servidor simulado (HTTP 500)',
    // Permissions
    camera_permission_required: 'Permissão da câmera necessária',
    location_permission_required: 'Permissão de localização necessária',
    contacts_permission_required: 'Permissão de contatos necessária',
    microphone_permission_required: 'Permissão do microfone necessária',
    // Location
    getting_location: 'Obtendo localização…',
    location_shared: 'Localização compartilhada',
    unable_to_get_location: 'Não foi possível obter a localização',
    shared_location: 'Localização compartilhada',
    // Chat
    new_chat: 'Novo chat',
    sync_contacts: 'Sincronizar contatos',
    allow_access_find_friends: 'Permitir acesso para encontrar amigos no NeuroComet',
    not_now: 'Agora não',
    allow: 'Permitir',
    contacts_synced: 'Contatos sincronizados',
    // Auth validation
    email_required: 'E-mail é obrigatório',
    invalid_email: 'Insira um endereço de e-mail válido',
    password_required: 'Senha é obrigatória',
    password_too_short: 'A senha deve ter pelo menos 6 caracteres',
    auth_required: 'Autenticação necessária',
  },
};

// Escape XML special characters
function escapeXml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
  // Note: single quotes (apostrophes) should be escaped with \' in Android strings.xml
  // The translations above already include \' where needed
}

// For each language, insert missing strings before </resources>
for (const [lang, trans] of Object.entries(translations)) {
  const filePath = path.join(resDir, `values-${lang}`, 'strings.xml');
  let content = fs.readFileSync(filePath, 'utf8');

  // Extract existing keys to avoid duplicates
  const existingKeys = new Set();
  const keyRegex = /name="([^"]+)"/g;
  let match;
  while ((match = keyRegex.exec(content)) !== null) {
    existingKeys.add(match[1]);
  }

  // Build new string entries
  const newEntries = [];
  for (const [key, value] of Object.entries(trans)) {
    if (!existingKeys.has(key)) {
      // The value is already escaped for XML apostrophes in the translations object
      newEntries.push(`    <string name="${key}">${value}</string>`);
    }
  }

  if (newEntries.length > 0) {
    // Insert before closing </resources> tag
    const insertBlock = '\n    <!-- Additional translations -->\n' + newEntries.join('\n') + '\n';
    content = content.replace('</resources>', insertBlock + '</resources>');
    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`${lang}: Added ${newEntries.length} translations`);
  } else {
    console.log(`${lang}: No new translations needed`);
  }
}

console.log('\nDone!');

