import { Component, OnInit } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { LoginModalService, Principal, Account } from 'app/core';
import { CourseService } from 'app/shared/service/CourseService';
import { CourseDto } from 'app/shared/model/course-dto.model';
import { CourseWithTNDto } from 'app/shared/model/courseWithTN-dto.model';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: ['home.css']
})
export class HomeComponent implements OnInit {
    account: Account;
    modalRef: NgbModalRef;
    classeNameNeedToReg: string;

    constructor(
        private principal: Principal,
        private loginModalService: LoginModalService,
        private eventManager: JhiEventManager,
        private courseService: CourseService
    ) {}

    courses: CourseDto[] = [];
    newCourse: CourseDto = new class implements CourseDto {
        courseContent: string = '';
        courseLocation: string = '';
        courseName: string = '';
        teacherId: string = '';
    }();

    coursesWithTN: CourseWithTNDto[] = [];

    ngOnInit() {
        this.principal.identity().then(account => {
            this.account = account;
        });
        this.registerAuthenticationSuccess();
    }

    registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', message => {
            this.principal.identity().then(account => {
                this.account = account;
            });
        });
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }

    getAllCourses() {
        this.courseService.getCourseInfo().subscribe(curDto => {
            if (!curDto) {
                this.courses = [];
            } else {
                this.courses = curDto;
                console.log(curDto);
            }
        });
    }

    getAllCoursesWithTN() {
        const userId = this.account['id'];
        this.courseService.getCourseInfoWithTN(userId).subscribe(curDto => {
            if (!curDto) {
                this.coursesWithTN = [];
            } else {
                this.coursesWithTN = curDto;
            }
        });
    }

    createCourse() {
        this.courseService.create(this.newCourse).subscribe(res => {
            if (res) {
                this.getAllCourses();
                this.clearInput();
            }
        });
    }

    clearInput() {
        this.newCourse.courseName = '';
        this.newCourse.teacherId = '';
        this.newCourse.courseContent = '';
        this.newCourse.courseLocation = '';
    }

    clearAllCourses() {
        this.courses = [];
    }

    clearAllCourseTN() {
        this.coursesWithTN = [];
    }

    deleteCourse(courseName) {
        this.courseService.delete(courseName).subscribe(res => {
            if (res) {
                this.getAllCourses();
            }
        });
    }

    addCourseToStudent(courseName) {
        // const currentUserCredential = this.account["id"];
        // this.courseService.addCourseToStudent(courseName, currentUserCredential);
        this.courseService.addCourseToStudent(courseName).subscribe(res => {
            if (res) {
                this.getAllCoursesWithTN();
            }
        });
    }
}
