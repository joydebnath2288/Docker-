const App = (function () {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^[0-9]{11}$/;

    function showAlert(containerId, type, html) {
        $('#' + containerId).html(
            '<div class="alert alert-' + type + ' alert-dismissible fade show d-flex align-items-center mb-4 shadow-sm" role="alert">' +
            '<div class="d-flex align-items-center">' + html + '</div>' +
            '<button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>' +
            '</div>'
        );
    }

    function initPasswordToggles() {
        $(document).on('click', '.btn-toggle-password', function () {
            const targetSelector = $(this).data('target');
            const input = $(targetSelector);
            const icon = $(this).find('i');
            if (input.attr('type') === 'password') {
                input.attr('type', 'text');
                icon.removeClass('bi-eye').addClass('bi-eye-slash');
            } else {
                input.attr('type', 'password');
                icon.removeClass('bi-eye-slash').addClass('bi-eye');
            }
        });
    }

    function initAutoClearValidation() {
        $(document).on('input change', 'input, select', function () {
            $(this).removeClass('is-invalid');
        });
    }

    function getCurrentSemester() {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth(); // 0-indexed: 0-3 = SPRING, 4-7 = SUMMER, 8-11 = FALL
        let term = 'FALL';
        if (month <= 3) {
            term = 'SPRING';
        } else if (month <= 7) {
            term = 'SUMMER';
        }
        return { term: term, year: year, text: term + ' ' + year };
    }

    function getAvailableTerms() {
        return ['SPRING', 'SUMMER', 'FALL'];
    }

    function getAvailableYears(range) {
        const currentYear = new Date().getFullYear();
        const span = (typeof range === 'number' && range > 0) ? range : 3;
        const years = [];
        for (let y = currentYear - span; y <= currentYear + span; y++) {
            years.push(y);
        }
        return years;
    }

    function initSemesterSelect(options) {
        const opts = options || {};
        const termSelect = $(opts.termSelector || '#semesterTerm');
        const yearSelect = $(opts.yearSelector || '#semesterYear');
        const hiddenInput = $(opts.hiddenSelector || '#semester');

        if (!termSelect.length && !yearSelect.length) return;

        const current = getCurrentSemester();
        let targetSemester = (opts.defaultValue || (hiddenInput.length ? hiddenInput.val() : '') || current.text).trim();

        let targetTerm = current.term;
        let targetYear = current.year;

        const parts = targetSemester.split(/\s+/);
        if (parts.length >= 2) {
            targetTerm = parts[0].toUpperCase();
            targetYear = parseInt(parts[1], 10) || current.year;
        }

        if (termSelect.length && termSelect.children('option').length <= 1) {
            let termHtml = '<option value="">Select Term</option>';
            getAvailableTerms().forEach(function (t) {
                termHtml += '<option value="' + t + '">' + t + '</option>';
            });
            termSelect.html(termHtml);
        }

        if (yearSelect.length && yearSelect.children('option').length <= 1) {
            let yearHtml = '<option value="">Select Year</option>';
            getAvailableYears(opts.yearRange || 3).forEach(function (y) {
                yearHtml += '<option value="' + y + '">' + y + '</option>';
            });
            yearSelect.html(yearHtml);
        }

        if (termSelect.length && targetTerm) {
            termSelect.val(targetTerm);
        }
        if (yearSelect.length && targetYear) {
            yearSelect.val(targetYear.toString());
        }

        function updateHidden() {
            const t = termSelect.val();
            const y = yearSelect.val();
            if (t && y) {
                const combined = t.trim().toUpperCase() + ' ' + y.trim();
                if (hiddenInput.length) hiddenInput.val(combined);
            }
        }

        updateHidden();
        termSelect.off('change.sem').on('change.sem', updateHidden);
        yearSelect.off('change.sem').on('change.sem', updateHidden);
    }

    function setupStudentForm(config) {
        const form = $(config.formSelector);
        if (!form.length) return;

        if (form.find('#semesterTerm').length || form.find('#semesterYear').length) {
            initSemesterSelect({
                termSelector: form.find('#semesterTerm'),
                yearSelector: form.find('#semesterYear'),
                hiddenSelector: form.find('#semester'),
                defaultValue: config.defaultValue
            });
        }

        form.on('submit', function (e) {
            e.preventDefault();

            let isValid = true;
            form.find('input, select').removeClass('is-invalid is-valid');

            const nameVal = form.find('#name').val().trim();
            const nameFeedback = form.find('#nameFeedback');
            if (!nameVal) {
                if (nameFeedback.length) nameFeedback.text("Please enter student's full name.");
                form.find('#name').addClass('is-invalid');
                isValid = false;
            } else if (/[0-9]/.test(nameVal)) {
                if (nameFeedback.length) nameFeedback.text("Name must not contain any numbers.");
                form.find('#name').addClass('is-invalid');
                isValid = false;
            } else {
                form.find('#name').addClass('is-valid');
            }

            const emailVal = form.find('#email').val().trim();
            if (!emailVal || !emailRegex.test(emailVal)) {
                form.find('#email').addClass('is-invalid');
                isValid = false;
            } else {
                form.find('#email').addClass('is-valid');
            }

            const phoneVal = form.find('#phone').val().trim();
            const phoneFeedback = form.find('#phoneFeedback');
            if (!phoneVal) {
                if (phoneFeedback.length) phoneFeedback.text("Please enter phone number.");
                form.find('#phone').addClass('is-invalid');
                isValid = false;
            } else if (!phoneRegex.test(phoneVal)) {
                if (phoneFeedback.length) phoneFeedback.text("Phone number must be exactly 11 digits and contain no letters.");
                form.find('#phone').addClass('is-invalid');
                isValid = false;
            } else {
                form.find('#phone').addClass('is-valid');
            }

            const deptVal = form.find('#department').val();
            if (!deptVal) {
                form.find('#department').addClass('is-invalid');
                isValid = false;
            } else {
                form.find('#department').addClass('is-valid');
            }

            const termVal = form.find('#semesterTerm').val();
            const yearVal = form.find('#semesterYear').val();
            const semesterHidden = form.find('#semester');
            let semesterVal = '';
            if (termVal && yearVal) {
                semesterVal = (termVal.trim().toUpperCase() + ' ' + yearVal.trim());
            } else if (semesterHidden.length) {
                semesterVal = semesterHidden.val().trim();
            }

            if (!semesterVal || !/^(SPRING|SUMMER|FALL)\s+\d{4}$/i.test(semesterVal)) {
                form.find('#semesterTerm, #semesterYear').addClass('is-invalid');
                isValid = false;
            } else {
                form.find('#semesterTerm, #semesterYear').addClass('is-valid');
                if (semesterHidden.length) semesterHidden.val(semesterVal);
            }

            const cgpaVal = form.find('#cgpa').val().trim();
            const cgpa = parseFloat(cgpaVal);
            if (isNaN(cgpa) || cgpa < 0.00 || cgpa > 4.00) {
                form.find('#cgpa').addClass('is-invalid');
                isValid = false;
            } else {
                form.find('#cgpa').addClass('is-valid');
            }

            const uidVal = form.find('#uid').length ? form.find('#uid').val().trim() : '';
            const idVal = form.find('#id').length ? form.find('#id').val().trim() : '';
            if (config.action === 'edit' && !idVal && !uidVal) {
                showAlert(config.alertContainerId || 'alertContainer', 'danger', 'Invalid student ID.');
                return;
            }

            if (!isValid) {
                showAlert(
                    config.alertContainerId || 'alertContainer',
                    'warning',
                    '<i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i> Please correct the highlighted fields before submitting.'
                );
                return;
            }

            const payload = {
                action: config.action,
                name: nameVal,
                email: emailVal,
                phone: phoneVal,
                department: deptVal,
                semester: semesterVal,
                cgpa: cgpaVal,
                username: form.find('#username').val().trim(),
                password: form.find('#password').val().trim()
            };
            if (idVal) {
                payload.id = idVal;
            }
            if (uidVal) {
                payload.uid = uidVal;
            }

            const $btn = form.find('#submitBtn');
            $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-2"></span>' + (config.loadingText || 'Saving...'));

            $.ajax({
                url: config.contextPath + '/api/students',
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(payload),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                success: function (response) {
                    if (response.success) {
                        showAlert(
                            config.alertContainerId || 'alertContainer',
                            'success',
                            '<i class="bi bi-check-circle-fill me-2 fs-5"></i> <strong>Success!</strong> ' + (response.message || 'Saved successfully.') + ' Redirecting...'
                        );
                        setTimeout(function () {
                            window.location.href = config.redirectUrl;
                        }, 1000);
                    } else {
                        showAlert(
                            config.alertContainerId || 'alertContainer',
                            'danger',
                            '<i class="bi bi-x-circle-fill me-2 fs-5"></i> ' + (response.message || 'Operation failed.')
                        );
                        $btn.prop('disabled', false).html(config.originalBtnHtml);
                    }
                },
                error: function (xhr) {
                    let errorMsg = 'Failed to submit form to server.';
                    try {
                        const res = JSON.parse(xhr.responseText);
                        if (res.message) errorMsg = res.message;
                    } catch (e) { }
                    showAlert(
                        config.alertContainerId || 'alertContainer',
                        'danger',
                        '<i class="bi bi-x-circle-fill me-2 fs-5"></i> <strong>Error:</strong> ' + errorMsg
                    );
                    $btn.prop('disabled', false).html(config.originalBtnHtml);
                }
            });
        });
    }

    $(document).ready(function () {
        initPasswordToggles();
        initAutoClearValidation();
        if ($('#semesterTerm').length || $('#semesterYear').length) {
            initSemesterSelect();
        }
    });

    return {
        showAlert: showAlert,
        setupStudentForm: setupStudentForm,
        getCurrentSemester: getCurrentSemester,
        getAvailableTerms: getAvailableTerms,
        getAvailableYears: getAvailableYears,
        initSemesterSelect: initSemesterSelect,
        emailRegex: emailRegex,
        phoneRegex: phoneRegex
    };
})();
