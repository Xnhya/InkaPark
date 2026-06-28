// main.js — IncaPark (seguro para Thymeleaf + HTML estático)
document.addEventListener("DOMContentLoaded", () => {
  /* ========= Marca link activo en navbar (rutas y .html) ========= */
  (() => {
    const links = document.querySelectorAll(".navbar .nav-link");
    if (!links.length) return;

    // pathname normalizado: "/" -> "inicio"
    const raw = location.pathname.replace(/\/+$/, "");       // quita "/" final
    const path = raw === "" || raw === "/" ? "inicio" : raw; // "/" => "inicio"

    links.forEach(a => {
      // Soporta href="/atracciones" y href="atracciones.html"
      const href = a.getAttribute("href") || a.getAttribute("th:href") || "";
      // Normaliza destino
      let dest = href.trim();

      // Si viene como Thymeleaf resuelto, será "/atracciones" o "/"
      // Si es archivo, será "atracciones.html"
      if (dest === "/" || dest === "/inicio") dest = "inicio";

      // Quita dominio, query y hash
      dest = dest.replace(location.origin, "").split("?")[0].split("#")[0];
      // "/atracciones" -> "atracciones"
      dest = dest.replace(/^\/+/, "");
      // "atracciones.html" -> "atracciones"
      dest = dest.replace(/\.html$/i, "");
      if (dest === "") dest = "inicio";

      // Normaliza path actual igual que dest
      let current = path.replace(/^\/+/, "").replace(/\.html$/i, "");
      if (current === "") current = "inicio";
      if (current === "tickets") current = "ticket"; // por si mezclas singular/plural

      if (dest === current) {
        a.classList.add("nav-current", "active");
        a.setAttribute("aria-current", "page");
      }
    });
  })();

  /* ========= Navbar: transparente -> sólida al hacer scroll ========= */
  (() => {
    const nav = document.querySelector(".navbar");
    if (!nav) return;

    const setState = () => {
      if (window.scrollY > 8) {
        nav.classList.remove("nav-transparent");
        nav.classList.add("nav-solid");
      } else {
        nav.classList.add("nav-transparent");
        nav.classList.remove("nav-solid");
      }
    };
    setState();
    window.addEventListener("scroll", setState, { passive: true });
  })();

  /* ========= Scroll suave para anchors ========= */
  document.addEventListener("click", (e) => {
    const a = e.target.closest('a[href^="#"]');
    if (!a) return;
    const id = a.getAttribute("href").slice(1);
    const el = document.getElementById(id);
    if (el) {
      e.preventDefault();
      el.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  });

  /* ========= Scroll Reveal con IntersectionObserver ========= */
  (() => {
    const els = document.querySelectorAll(".reveal");
    if (!els.length) return;

    if (!("IntersectionObserver" in window)) {
      els.forEach(el => el.classList.add("revealed"));
      return;
    }

    const io = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add("revealed");
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15 });

    els.forEach(el => io.observe(el));
  })();

  /* ========= Hero slider (si hay múltiples <img> dentro de .hero-media) ========= */
  (() => {
    const media = document.querySelector(".hero-media");
    if (!media) return;
    const imgs = Array.from(media.querySelectorAll("img"));
    if (imgs.length <= 1) return;

    media.style.position = "relative";
    imgs.forEach((img, i) => {
      img.style.position = "absolute";
      img.style.inset = "0";
      img.style.width = "100%";
      img.style.height = "100%";
      img.style.objectFit = "cover";
      img.style.opacity = i === 0 ? "1" : "0";
      img.style.transition = "opacity 1.1s ease-in-out";
    });

    let idx = 0;
    setInterval(() => {
      const current = imgs[idx];
      idx = (idx + 1) % imgs.length;
      const next = imgs[idx];
      current.style.opacity = "0";
      next.style.opacity = "1";
    }, 8000);
  })();

  /* ========= Validación Bootstrap ========= */
  (() => {
    const forms = document.querySelectorAll(".needs-validation");
    if (!forms.length) return;
    Array.prototype.slice.call(forms).forEach((form) => {
      form.addEventListener("submit", (event) => {
        if (!form.checkValidity()) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add("was-validated");
      }, false);
    });
  })();

  /* ========= Toggle contraseña (delegado, sirve en cualquier página) ========= */
  document.addEventListener("click", (e) => {
    const btn = e.target.closest(".password-toggle");
    if (!btn) return;

    const targetId = btn.getAttribute("data-target");
    const input = document.getElementById(targetId);
    if (!input) return;

    const icon = btn.querySelector("i");
    if (input.type === "password") {
      input.type = "text";
      if (icon) { icon.classList.remove("fa-eye"); icon.classList.add("fa-eye-slash"); }
    } else {
      input.type = "password";
      if (icon) { icon.classList.remove("fa-eye-slash"); icon.classList.add("fa-eye"); }
    }
    input.focus();
  });
});
