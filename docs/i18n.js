(function () {
  var SUPPORTED = ['en', 'it', 'fr', 'es', 'de'];

  function buildSwitcher() {
    return '<div class="lang-switcher">' +
      SUPPORTED.map(function (l) {
        return '<button class="lang-btn" data-lang="' + l + '">' + l.toUpperCase() + '</button>';
      }).join('') +
      '</div>';
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.lang-switcher-slot').forEach(function (slot) {
      slot.innerHTML = buildSwitcher();
    });

    var currentLang = document.documentElement.lang || 'en';
    document.querySelectorAll('.lang-btn').forEach(function (btn) {
      btn.classList.toggle('active', btn.getAttribute('data-lang') === currentLang);
      btn.addEventListener('click', function () {
        var lang = btn.getAttribute('data-lang');
        if (window.langUrls && window.langUrls[lang]) {
          window.location.href = window.langUrls[lang];
        }
      });
    });
  });
})();
