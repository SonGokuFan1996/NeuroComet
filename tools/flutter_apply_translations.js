const fs = require('fs');
const path = require('path');

const dartFile = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'flutter_app', 'lib', 'l10n', 'app_localizations.dart');
const outFile = path.join('C:', 'Users', 'bkyil', 'AndroidStudioProjects', 'NeuroComet', 'tools', 'flutter_apply_log.txt');

let content = fs.readFileSync(dartFile, 'utf8');
let log = '';

// The 114 missing keys translations for each language
const translations = {
  tr: {
    // Account Management
    deleteAccount: 'Hesabı Sil',
    deleteAccountConfirm: 'Hesabınızı silmek istediğinizden emin misiniz?',
    deleteAccountWarning: 'Bu işlem geri alınamaz. Tüm verileriniz kalıcı olarak silinecektir.',
    deleteAccountScheduled: 'Hesap 14 gün içinde silinmek üzere planlandı. İptal etmek için tekrar giriş yapın.',
    cancelDeletion: 'Silmeyi İptal Et',
    accountDeletionCancelled: 'Hesap silme iptal edildi',
    emailVerification: 'E-posta Doğrulama',
    resendVerification: 'Doğrulama E-postasını Tekrar Gönder',
    verificationSent: 'Doğrulama e-postası gönderildi!',
    emailNotVerified: 'E-postanız doğrulanmamış',
    verifyEmail: 'E-postayı Doğrula',
    updatePassword: 'Şifreyi Güncelle',
    passwordUpdated: 'Şifre başarıyla güncellendi',
    updateEmail: 'E-postayı Güncelle',
    currentPassword: 'Mevcut Şifre',
    newPassword: 'Yeni Şifre',
    // Safety & Moderation
    userBlocked: 'Kullanıcı engellendi',
    userUnblocked: 'Kullanıcının engeli kaldırıldı',
    userMuted: 'Kullanıcı susturuldu',
    userUnmuted: 'Kullanıcının sesi açıldı',
    reportSubmitted: 'Rapor gönderildi. En kısa sürede inceleyeceğiz.',
    reportReason: 'Raporlama nedeni',
    reportDetails: 'Ek ayrıntılar (isteğe bağlı)',
    reportSpam: 'Spam',
    reportHarassment: 'Taciz',
    reportHateSpeech: 'Nefret söylemi',
    reportMisinformation: 'Yanlış bilgi',
    reportViolence: 'Şiddet',
    reportOther: 'Diğer',
    unblock: 'Engeli Kaldır',
    unmute: 'Sesi Aç',
    blockedUsersEmpty: 'Engellenen kullanıcı yok',
    mutedUsersEmpty: 'Sessize alınan kullanıcı yok',
    // Bookmarks
    bookmark: 'Yer İmi',
    bookmarkAdded: 'Gönderi yer imlerine eklendi',
    bookmarkRemoved: 'Yer imi kaldırıldı',
    bookmarks: 'Yer İmleri',
    noBookmarks: 'Henüz yer imi yok',
    bookmarksHint: 'Yer imlerine eklediğiniz gönderiler burada görünecek.',
    // Beta & Feedback
    betaWelcome: 'Beta\\\'ya Hoş Geldiniz! 🚀',
    betaWelcomeMessage: 'NeuroComet\\\'i test ettiğiniz için teşekkürler! Geri bildirimleriniz daha iyi bir uygulama oluşturmamıza yardımcı oluyor.',
    sendFeedback: 'Geri Bildirim Gönder',
    bugReport: 'Hata Bildir',
    featureRequest: 'Özellik İsteği',
    betaVersion: 'Beta Sürümü',
    // Errors & Status
    connectionError: 'Bağlantı hatası. Lütfen internetinizi kontrol edin.',
    serverError: 'Sunucu hatası. Lütfen daha sonra tekrar deneyin.',
    unknownError: 'Bir şeyler ters gitti. Lütfen tekrar deneyin.',
    offline: 'Çevrimdışısınız',
    online: 'Tekrar çevrimiçi',
    sessionExpired: 'Oturum süresi doldu. Lütfen tekrar giriş yapın.',
    rateLimited: 'Çok fazla istek. Lütfen bir süre bekleyin.',
    noInternet: 'İnternet bağlantısı yok',
    tryAgain: 'Tekrar Dene',
    dismiss: 'Kapat',
    // Search
    searchUsers: 'Kullanıcı ara',
    searchPosts: 'Gönderi ara',
    noResults: 'Sonuç bulunamadı',
    searchHint: 'Farklı bir arama terimi deneyin',
    // Profile
    posts: 'Gönderiler',
    aboutMe: 'Hakkımda',
    interests: 'İlgi Alanları',
    badges: 'Rozetler',
    noBio: 'Henüz biyografi yok',
    editBio: 'Biyografiyi düzenle',
    pronouns: 'Zamirler',
    joinedDate: '{date} tarihinde katıldı',
    lastSeen: 'Son görülme {time}',
    // Post Actions
    copyLink: 'Bağlantıyı Kopyala',
    linkCopied: 'Bağlantı panoya kopyalandı',
    sharePost: 'Gönderiyi Paylaş',
    hidePost: 'Gönderiyi Gizle',
    postHidden: 'Gönderi gizlendi',
    reportPost: 'Gönderiyi Şikayet Et',
    deletePost: 'Gönderiyi Sil',
    deletePostConfirm: 'Bu gönderiyi silmek istediğinizden emin misiniz?',
    postDeleted: 'Gönderi silindi',
    // Comments
    comments: 'Yorumlar',
    addComment: 'Yorum ekle...',
    noComments: 'Henüz yorum yok',
    beFirstToComment: 'İlk yorumu yapan siz olun!',
    replyTo: '{name} adlı kişiye yanıt',
    viewReplies: '{count} yanıtı görüntüle',
    hideReplies: 'Yanıtları gizle',
    // Notifications
    notificationLike: '{name} gönderinizi beğendi',
    notificationComment: '{name} gönderinize yorum yaptı',
    notificationFollow: '{name} sizi takip etmeye başladı',
    notificationMention: '{name} sizi etiketledi',
    notificationMessage: '{name} size mesaj gönderdi',
    today: 'Bugün',
    yesterday: 'Dün',
    thisWeek: 'Bu Hafta',
    earlier: 'Daha Önce',
    unread: 'Okunmamış',
    mentions: 'Etiketlemeler',
    likes: 'Beğeniler',
    follows: 'Takipler',
    // Time
    secondsAgo: '{n}sn önce',
    minuteAgo: '1 dakika önce',
    hourAgo: '1 saat önce',
    dayAgo: '1 gün önce',
    weekAgo: '1 hafta önce',
    monthAgo: '1 ay önce',
    yearAgo: '1 yıl önce',
    // Accessibility
    skipToContent: 'İçeriğe atla',
    closeDialog: 'İletişim kutusunu kapat',
    openMenu: 'Menüyü aç',
    closeMenu: 'Menüyü kapat',
    goBack: 'Geri git',
    scrollToTop: 'Yukarı kaydır',
    loadMore: 'Daha fazla yükle',
    refreshContent: 'İçeriği yenile',
    imageDescription: 'Resim açıklaması',
    videoDescription: 'Video açıklaması',
  },

  fr: {
    // Account Management
    deleteAccount: 'Supprimer le compte',
    deleteAccountConfirm: 'Êtes-vous sûr de vouloir supprimer votre compte ?',
    deleteAccountWarning: 'Cette action est irréversible. Toutes vos données seront définitivement supprimées.',
    deleteAccountScheduled: 'Compte programmé pour suppression dans 14 jours. Reconnectez-vous pour annuler.',
    cancelDeletion: 'Annuler la suppression',
    accountDeletionCancelled: 'Suppression du compte annulée',
    emailVerification: 'Vérification de l\\\'e-mail',
    resendVerification: 'Renvoyer l\\\'e-mail de vérification',
    verificationSent: 'E-mail de vérification envoyé !',
    emailNotVerified: 'Votre e-mail n\\\'est pas vérifié',
    verifyEmail: 'Vérifier l\\\'e-mail',
    updatePassword: 'Mettre à jour le mot de passe',
    passwordUpdated: 'Mot de passe mis à jour avec succès',
    updateEmail: 'Mettre à jour l\\\'e-mail',
    currentPassword: 'Mot de passe actuel',
    newPassword: 'Nouveau mot de passe',
    // Safety & Moderation
    userBlocked: 'Utilisateur bloqué',
    userUnblocked: 'Utilisateur débloqué',
    userMuted: 'Utilisateur mis en sourdine',
    userUnmuted: 'Sourdine désactivée',
    reportSubmitted: 'Signalement envoyé. Nous l\\\'examinerons sous peu.',
    reportReason: 'Raison du signalement',
    reportDetails: 'Détails supplémentaires (facultatif)',
    reportSpam: 'Spam',
    reportHarassment: 'Harcèlement',
    reportHateSpeech: 'Discours haineux',
    reportMisinformation: 'Désinformation',
    reportViolence: 'Violence',
    reportOther: 'Autre',
    unblock: 'Débloquer',
    unmute: 'Réactiver le son',
    blockedUsersEmpty: 'Aucun utilisateur bloqué',
    mutedUsersEmpty: 'Aucun utilisateur en sourdine',
    // Bookmarks
    bookmark: 'Signet',
    bookmarkAdded: 'Publication ajoutée aux signets',
    bookmarkRemoved: 'Signet supprimé',
    bookmarks: 'Signets',
    noBookmarks: 'Aucun signet pour le moment',
    bookmarksHint: 'Les publications que vous mettez en signet apparaîtront ici.',
    // Beta & Feedback
    betaWelcome: 'Bienvenue dans la Bêta ! 🚀',
    betaWelcomeMessage: 'Merci de tester NeuroComet ! Vos commentaires nous aident à construire une meilleure application.',
    sendFeedback: 'Envoyer des commentaires',
    bugReport: 'Signaler un bug',
    featureRequest: 'Demander une fonctionnalité',
    betaVersion: 'Version Bêta',
    // Errors & Status
    connectionError: 'Erreur de connexion. Veuillez vérifier votre connexion Internet.',
    serverError: 'Erreur du serveur. Veuillez réessayer plus tard.',
    unknownError: 'Une erreur s\\\'est produite. Veuillez réessayer.',
    offline: 'Vous êtes hors ligne',
    online: 'De retour en ligne',
    sessionExpired: 'Session expirée. Veuillez vous reconnecter.',
    rateLimited: 'Trop de requêtes. Veuillez patienter un instant.',
    noInternet: 'Pas de connexion Internet',
    tryAgain: 'Réessayer',
    dismiss: 'Ignorer',
    // Search
    searchUsers: 'Rechercher des utilisateurs',
    searchPosts: 'Rechercher des publications',
    noResults: 'Aucun résultat trouvé',
    searchHint: 'Essayez un autre terme de recherche',
    // Profile
    posts: 'Publications',
    aboutMe: 'À propos de moi',
    interests: 'Centres d\\\'intérêt',
    badges: 'Badges',
    noBio: 'Pas encore de biographie',
    editBio: 'Modifier la biographie',
    pronouns: 'Pronoms',
    joinedDate: 'Inscrit le {date}',
    lastSeen: 'Dernière connexion {time}',
    // Post Actions
    copyLink: 'Copier le lien',
    linkCopied: 'Lien copié dans le presse-papiers',
    sharePost: 'Partager la publication',
    hidePost: 'Masquer la publication',
    postHidden: 'Publication masquée',
    reportPost: 'Signaler la publication',
    deletePost: 'Supprimer la publication',
    deletePostConfirm: 'Êtes-vous sûr de vouloir supprimer cette publication ?',
    postDeleted: 'Publication supprimée',
    // Comments
    comments: 'Commentaires',
    addComment: 'Ajouter un commentaire...',
    noComments: 'Aucun commentaire pour le moment',
    beFirstToComment: 'Soyez le premier à commenter !',
    replyTo: 'Répondre à {name}',
    viewReplies: 'Voir {count} réponses',
    hideReplies: 'Masquer les réponses',
    // Notifications
    notificationLike: '{name} a aimé votre publication',
    notificationComment: '{name} a commenté votre publication',
    notificationFollow: '{name} a commencé à vous suivre',
    notificationMention: '{name} vous a mentionné',
    notificationMessage: '{name} vous a envoyé un message',
    today: 'Aujourd\\\'hui',
    yesterday: 'Hier',
    thisWeek: 'Cette semaine',
    earlier: 'Plus tôt',
    unread: 'Non lu',
    mentions: 'Mentions',
    likes: 'J\\\'aime',
    follows: 'Suivis',
    // Time
    secondsAgo: 'il y a {n}s',
    minuteAgo: 'il y a 1 minute',
    hourAgo: 'il y a 1 heure',
    dayAgo: 'il y a 1 jour',
    weekAgo: 'il y a 1 semaine',
    monthAgo: 'il y a 1 mois',
    yearAgo: 'il y a 1 an',
    // Accessibility
    skipToContent: 'Passer au contenu',
    closeDialog: 'Fermer la boîte de dialogue',
    openMenu: 'Ouvrir le menu',
    closeMenu: 'Fermer le menu',
    goBack: 'Retour',
    scrollToTop: 'Défiler vers le haut',
    loadMore: 'Charger plus',
    refreshContent: 'Actualiser le contenu',
    imageDescription: 'Description de l\\\'image',
    videoDescription: 'Description de la vidéo',
  },

  pt: {
    // Account Management
    deleteAccount: 'Excluir conta',
    deleteAccountConfirm: 'Tem certeza de que deseja excluir sua conta?',
    deleteAccountWarning: 'Esta ação não pode ser desfeita. Todos os seus dados serão excluídos permanentemente.',
    deleteAccountScheduled: 'Conta programada para exclusão em 14 dias. Faça login novamente para cancelar.',
    cancelDeletion: 'Cancelar exclusão',
    accountDeletionCancelled: 'Exclusão da conta cancelada',
    emailVerification: 'Verificação de e-mail',
    resendVerification: 'Reenviar e-mail de verificação',
    verificationSent: 'E-mail de verificação enviado!',
    emailNotVerified: 'Seu e-mail não foi verificado',
    verifyEmail: 'Verificar e-mail',
    updatePassword: 'Atualizar senha',
    passwordUpdated: 'Senha atualizada com sucesso',
    updateEmail: 'Atualizar e-mail',
    currentPassword: 'Senha atual',
    newPassword: 'Nova senha',
    // Safety & Moderation
    userBlocked: 'Usuário bloqueado',
    userUnblocked: 'Usuário desbloqueado',
    userMuted: 'Usuário silenciado',
    userUnmuted: 'Som do usuário reativado',
    reportSubmitted: 'Denúncia enviada. Analisaremos em breve.',
    reportReason: 'Motivo da denúncia',
    reportDetails: 'Detalhes adicionais (opcional)',
    reportSpam: 'Spam',
    reportHarassment: 'Assédio',
    reportHateSpeech: 'Discurso de ódio',
    reportMisinformation: 'Desinformação',
    reportViolence: 'Violência',
    reportOther: 'Outro',
    unblock: 'Desbloquear',
    unmute: 'Reativar som',
    blockedUsersEmpty: 'Nenhum usuário bloqueado',
    mutedUsersEmpty: 'Nenhum usuário silenciado',
    // Bookmarks
    bookmark: 'Favorito',
    bookmarkAdded: 'Publicação adicionada aos favoritos',
    bookmarkRemoved: 'Favorito removido',
    bookmarks: 'Favoritos',
    noBookmarks: 'Nenhum favorito ainda',
    bookmarksHint: 'Publicações que você adicionar aos favoritos aparecerão aqui.',
    // Beta & Feedback
    betaWelcome: 'Bem-vindo ao Beta! 🚀',
    betaWelcomeMessage: 'Obrigado por testar o NeuroComet! Seu feedback nos ajuda a construir um aplicativo melhor.',
    sendFeedback: 'Enviar feedback',
    bugReport: 'Reportar um bug',
    featureRequest: 'Solicitar uma funcionalidade',
    betaVersion: 'Versão Beta',
    // Errors & Status
    connectionError: 'Erro de conexão. Verifique sua internet.',
    serverError: 'Erro no servidor. Tente novamente mais tarde.',
    unknownError: 'Algo deu errado. Tente novamente.',
    offline: 'Você está offline',
    online: 'De volta online',
    sessionExpired: 'Sessão expirada. Faça login novamente.',
    rateLimited: 'Muitas solicitações. Aguarde um momento.',
    noInternet: 'Sem conexão com a internet',
    tryAgain: 'Tentar novamente',
    dismiss: 'Dispensar',
    // Search
    searchUsers: 'Pesquisar usuários',
    searchPosts: 'Pesquisar publicações',
    noResults: 'Nenhum resultado encontrado',
    searchHint: 'Tente um termo de pesquisa diferente',
    // Profile
    posts: 'Publicações',
    aboutMe: 'Sobre mim',
    interests: 'Interesses',
    badges: 'Emblemas',
    noBio: 'Nenhuma biografia ainda',
    editBio: 'Editar biografia',
    pronouns: 'Pronomes',
    joinedDate: 'Entrou em {date}',
    lastSeen: 'Visto por último {time}',
    // Post Actions
    copyLink: 'Copiar link',
    linkCopied: 'Link copiado para a área de transferência',
    sharePost: 'Compartilhar publicação',
    hidePost: 'Ocultar publicação',
    postHidden: 'Publicação ocultada',
    reportPost: 'Denunciar publicação',
    deletePost: 'Excluir publicação',
    deletePostConfirm: 'Tem certeza de que deseja excluir esta publicação?',
    postDeleted: 'Publicação excluída',
    // Comments
    comments: 'Comentários',
    addComment: 'Adicionar um comentário...',
    noComments: 'Nenhum comentário ainda',
    beFirstToComment: 'Seja o primeiro a comentar!',
    replyTo: 'Responder a {name}',
    viewReplies: 'Ver {count} respostas',
    hideReplies: 'Ocultar respostas',
    // Notifications
    notificationLike: '{name} curtiu sua publicação',
    notificationComment: '{name} comentou sua publicação',
    notificationFollow: '{name} começou a seguir você',
    notificationMention: '{name} mencionou você',
    notificationMessage: '{name} enviou uma mensagem',
    today: 'Hoje',
    yesterday: 'Ontem',
    thisWeek: 'Esta semana',
    earlier: 'Anteriormente',
    unread: 'Não lido',
    mentions: 'Menções',
    likes: 'Curtidas',
    follows: 'Seguimentos',
    // Time
    secondsAgo: '{n}s atrás',
    minuteAgo: '1 minuto atrás',
    hourAgo: '1 hora atrás',
    dayAgo: '1 dia atrás',
    weekAgo: '1 semana atrás',
    monthAgo: '1 mês atrás',
    yearAgo: '1 ano atrás',
    // Accessibility
    skipToContent: 'Pular para o conteúdo',
    closeDialog: 'Fechar diálogo',
    openMenu: 'Abrir menu',
    closeMenu: 'Fechar menu',
    goBack: 'Voltar',
    scrollToTop: 'Rolar para o topo',
    loadMore: 'Carregar mais',
    refreshContent: 'Atualizar conteúdo',
    imageDescription: 'Descrição da imagem',
    videoDescription: 'Descrição do vídeo',
  },
};

// For each language, find the closing }, of its block and insert before it
for (const [lang, trans] of Object.entries(translations)) {
  // Find the start of this language's map
  const langPattern = new RegExp(`'${lang}':\\s*\\{`);
  const match = langPattern.exec(content);
  if (!match) {
    log += `${lang}: NOT FOUND in file\n`;
    continue;
  }

  // Find the matching closing brace
  let braceCount = 0;
  let startIdx = match.index + match[0].length - 1;
  let endIdx = startIdx;
  for (let i = startIdx; i < content.length; i++) {
    if (content[i] === '{') braceCount++;
    if (content[i] === '}') braceCount--;
    if (braceCount === 0) {
      endIdx = i;
      break;
    }
  }

  // Check which keys already exist
  const block = content.substring(startIdx, endIdx);
  const existingKeys = new Set();
  const keyRegex = /'([a-zA-Z_][a-zA-Z0-9_]*)'\s*:/g;
  let km;
  while ((km = keyRegex.exec(block)) !== null) {
    existingKeys.add(km[1]);
  }

  // Build new entries
  const newEntries = [];
  for (const [key, value] of Object.entries(trans)) {
    if (!existingKeys.has(key)) {
      newEntries.push(`      '${key}': '${value}',`);
    }
  }

  if (newEntries.length > 0) {
    // Insert before the closing } of this language's block
    const insertText = '\n      // Additional translations\n' + newEntries.join('\n') + '\n';
    // Insert just before the closing }
    content = content.substring(0, endIdx) + insertText + content.substring(endIdx);
    log += `${lang}: Added ${newEntries.length} translations\n`;
  } else {
    log += `${lang}: No new translations needed\n`;
  }
}

fs.writeFileSync(dartFile, content, 'utf8');
log += '\nDone! File updated.\n';
fs.writeFileSync(outFile, log, 'utf8');
console.log(log);

